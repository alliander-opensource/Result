package com.alliander.example.sources

import com.alliander.example.*
import com.alliander.result.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.*
import io.kotest.matchers.Matcher
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import io.mockk.*

class EvenSourceTest: StringSpec({
    lateinit var baseSource: Source

    beforeTest {
        baseSource = mockk()
    }

    "if the base source fails to produce an integer, so does EvenSource" {
        checkAll(sourceErrors) { problem ->
            every { baseSource.integer() }.returns(Failure(problem))
            val source = EvenSource(baseSource)

            source.integer() shouldBe Failure(problem)
        }
    }

    "EvenSource only produces even integers" {
        checkAll<Int> { number ->
            every { baseSource.integer() }.returns(Success(number))
            val source = EvenSource(baseSource)

            val result = source.integer()

            result shouldBe aSuccess()
            result.map{n ->isEven(n)}.withDefault(false) shouldBe true
        }
    }
})

val sourceErrors = listOf(*SourceError.values()).exhaustive()

fun isEven(n:Int): Boolean = n % 2 == 0

fun aSuccess() = object : Matcher<Result<*, *>> {
    override fun test(result: Result<*, *>): MatcherResult {
        return MatcherResult(
                result.map {true}.withDefault(false),
                "$result should be a Success, but was a Failure",
                "$result should not be a Success, but it was"
        )
    }
}

