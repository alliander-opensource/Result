package com.alliander.example.dice

import com.alliander.example.Source
import com.alliander.example.SourceError
import com.alliander.example.extentions.modulo
import com.alliander.example.networks.RandomOrg
import com.alliander.example.sources.NetworkSource
import com.alliander.result.Failure
import com.alliander.result.Result
import com.alliander.result.Success
import java.net.http.HttpClient
import java.util.regex.Pattern

private val dicePattern: Pattern = Pattern.compile("^([1-9]\\d*)d([1-9]\\d*)$")
fun String.toDice(): Result<DiceError, Dice> {
    val matcher = dicePattern.matcher(this)
    return if (matcher.matches()) {
        val number = matcher.group(1).toInt()
        val faces = matcher.group(2).toInt()
        return Success(Dice(number, faces))
    } else {
        Failure(ParseError(this))
    }
}

sealed class DiceError(val kind: String)
data class ParseError(val input: String) : DiceError("ParseError")
data class RandomSourceError(val error: SourceError) : DiceError("SourceError")

data class Dice(val source: Source, val number: Int, val faces: Int) {
    constructor(number: Int, faces: Int) : this(NetworkSource(RandomOrg(HttpClient.newHttpClient())), number, faces)

    fun roll(): Result<DiceError, Int> {
        return List(number) { single() }
            .fold(Success(emptyList()), ::combine)
            .map { xs -> xs.sum() }
            .mapError { error -> error as DiceError }
    }

    private fun single(): Result<RandomSourceError, Int> {
        return source.integer()
            .mapError(::RandomSourceError)
            .map { n -> n.modulo(faces) }
            .map { n -> n + 1 }
    }
}

fun combine(
    accumulator: Result<RandomSourceError, List<Int>>,
    element: Result<RandomSourceError, Int>
): Result<RandomSourceError, List<Int>> {
    return accumulator.andThen { xs ->
        element.map { x -> xs + x }
    }
}
