package com.alliander.example.networks

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.exactly
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.checkAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.http.HttpClient
import java.net.http.HttpResponse

class RandomOrgTest: StringSpec({
    lateinit var client: HttpClient

    beforeAny {
        client = mockk(relaxed = true)
    }

    "when fetching client is used" {
        checkAll(configurations) { configuration ->
            val source = RandomOrg(configuration, client)

            source.fetch()

            verify(exactly = 1) { client.send(configuration.toRequest(), HttpResponse.BodyHandlers.ofString()) }
        }
    }
})

val configurations = arbitrary { rs: RandomSource ->
    val number = rs.random.nextInt()
    val minimum = rs.random.nextInt()
    val maximum = rs.random.nextInt()
    Configuration(number, minimum, maximum)
}
