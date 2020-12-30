package com.alliander.result

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class ExtensionsTest: StringSpec({
    "if a nullable type is not null toResult is Success" {
        checkAll { value: Boolean ->
            val result = value.toResult()

            result shouldBe Success(value)
        }
    }

    "if a nullable type is null toResult is Failure" {
        val value: Boolean? = null

        val result = value.toResult()

        result shouldBe Failure(Unit)
    }
})
