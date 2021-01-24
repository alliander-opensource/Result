package com.alliander.example.networks

import com.alliander.example.*
import com.alliander.result.*
import java.net.URI
import java.net.http.*

val defaultConfiguration = Configuration(100, 1, 100)

data class RandomOrg(private val configuration: Configuration, private val parser: BodyParser, private val client: HttpClient): Network {
    constructor(configuration: Configuration, client: HttpClient): this(configuration, BodyParser(), client)
    constructor(parser: BodyParser, client: HttpClient): this(defaultConfiguration, parser, client)
    constructor(client: HttpClient): this(defaultConfiguration, client)

    override fun fetch(): Result<NetworkError, List<Int>> {
        val response = client.send(configuration.toRequest(), HttpResponse.BodyHandlers.ofString())
        return response.bodyToResult()
                .andThen(parser::parse)
    }
}

private fun HttpResponse<String>.bodyToResult(): Result<NetworkError, String> {
    return if (this.statusCode() == 200) {
        Success(this.body())
    } else {
        Failure(NetworkError.NotOK)
    }
}

data class Configuration(val number: Int, val minimum: Int, val maximum: Int) {
    fun toRequest(): HttpRequest {
        return HttpRequest.newBuilder()
                .uri(URI.create("https://www.random.org/integers/?num=$number&min=$minimum&max=$maximum&col=1&base=10&format=plain&rnd=new"))
                .GET()
                .build()
    }
}

class BodyParser {
    fun parse(input: String): Result<NetworkError, List<Int>> {
        if (input.isBlank()) {return Success(emptyList())}
        return try {
            input.trimEnd()
                    .split("\n")
                    .map(Integer::parseInt)
                    .toResult()
                    .mapError { NetworkError.Generic }
        } catch(e: NumberFormatException) {
            Failure(NetworkError.NotAnInteger)
        }
    }
}
