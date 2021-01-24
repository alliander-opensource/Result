package com.alliander.example.networks

import com.alliander.result.Success
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class RandomOrgBodyParserTest: StringSpec({
    "any collection of integers separated by newlines parses" {
        checkAll<List<Int>> { numbers ->
            val input = numbers.joinToString(separator = "\n", postfix = "\n")
            val parser = BodyParser()

            parser.parse(input) shouldBe Success(numbers)
        }
    }
})
