package com.alliander.example.networks

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import java.net.URI
import java.net.http.HttpRequest

class RandomOrgConfigurationTest: StringSpec({
    "configuration can turn into a request" {
        checkAll<Int, Int, Int> { number, minimum, maximum ->
            val configuration = Configuration(number, minimum, maximum)
            val expectedRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.random.org/integers/?num=$number&min=$minimum&max=$maximum&col=1&base=10&format=plain&rnd=new"))
                    .GET()
                    .build()

            configuration.toRequest() shouldBe expectedRequest
        }
    }
})
