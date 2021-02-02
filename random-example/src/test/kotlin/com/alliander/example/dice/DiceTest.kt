package com.alliander.example.dice

import com.alliander.example.*
import com.alliander.example.generators.integers
import com.alliander.result.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.*
import io.kotest.matchers.Matcher
import io.kotest.matchers.ints.*
import io.kotest.property.*
import io.mockk.*

class DiceTest : StringSpec({
    "dice can be created by their specification" {
        checkAll(
            com.alliander.example.generators.positive(1),
            com.alliander.example.generators.positive(2, 100)
        ) { number, faces ->
            val dice = "${number}d${faces}".toDice()

            dice shouldBe successful<DiceError, Int>()
            dice.map(Dice::number) shouldBe Success(number)
            dice.map(Dice::faces) shouldBe Success(faces)
        }
    }

    "each number of dice request an integers from the source" {
        checkAll(
            com.alliander.example.generators.positive(1, 5),
            com.alliander.example.generators.positive(2, 100)
        ) { number, faces ->
            val source: Source = mockk(relaxed = true)
            val dice = Dice(source, number, faces)

            dice.roll()

            verify(exactly = number) { source.integer() }
        }
    }

    "each roll is between 1 and the number of faces" {
        checkAll(com.alliander.example.generators.positive(2), integers()) { faces, integer ->
            val source: Source = mockk(relaxed = true)
            every { source.integer() }.returns(Success(integer))
            val dice = Dice(source, 1, faces)

            val roll = dice.roll().withDefault(0)

            roll shouldBeGreaterThanOrEqual 1
            roll shouldBeLessThanOrEqual faces
        }
    }
})

fun <E, T> successful() = object: Matcher<Result<E, T>> {
    override fun test(value: Result<E, T>): MatcherResult {
        return MatcherResult(value.map{_ -> true}.withDefault(false), "$value should be Success", "$value should not be Success")
    }
}
