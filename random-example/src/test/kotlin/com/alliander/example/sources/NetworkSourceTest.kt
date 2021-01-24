package com.alliander.example.sources

import com.alliander.example.Network
import com.alliander.example.NetworkError
import com.alliander.example.SourceError
import com.alliander.result.Failure
import com.alliander.result.Success
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class NetworkSourceTest : StringSpec({
    lateinit var network: Network

    beforeTest {
        network = mockk(relaxed = true)
    }

    "when batch is empty, random numbers are fetched from network" {
        val source = NetworkSource(network, emptyList())

        source.integer()

        verify(exactly = 1) { network.fetch() }
    }

    "when batch is non-empty, random numbers are not fetched from network" {
        val source = NetworkSource(network, listOf(ANY_NUMBER))

        source.integer()

        verify(exactly = 0) { network.fetch() }
    }

    "when numbers are fetched, first number of batch is returned" {
        checkAll<Int> { number ->
            every {network.fetch()}.returns(Success(listOf(number, ANY_NUMBER)))
            val source = NetworkSource(network, emptyList())

            source.integer() shouldBe Success(number)
        }
    }

    "when batch is non-empty, first element of batch is returned" {
        checkAll<Int> { number ->
            val source = NetworkSource(network, listOf(number, ANY_NUMBER))

            source.integer() shouldBe Success(number)
        }
    }

    "when fetching numbers fails, failure is returned" {
        checkAll(networkErrors) { error ->
            every {network.fetch()}.returns(Failure(error))
            val source = NetworkSource(network, emptyList())

            source.integer() shouldBe Failure(SourceError.Empty)
        }
    }


    "when batch is non-empty, elements are returnes consecutively" {
        checkAll<Int, Int> { first, second ->
            val source = NetworkSource(network, listOf(first, second))

            source.integer() shouldBe Success(first)
            source.integer() shouldBe Success(second)
        }
    }
})

val ANY_NUMBER = 37
val networkErrors = listOf(*NetworkError.values()).exhaustive()
