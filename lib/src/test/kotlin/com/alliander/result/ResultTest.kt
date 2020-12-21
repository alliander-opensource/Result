package com.alliander.result

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.*
import io.kotest.property.checkAll

class ResultTest: StringSpec({
    "Success withDefault returns actual value" {
        checkAll { value: Int ->
            Success<Unit, Int>(value).withDefault(0) shouldBe value
        }
    }

    "Failure withDefault returns default value" {
        checkAll { defaultValue: Int ->
            Failure<Unit, Int>(Unit).withDefault(defaultValue) shouldBe defaultValue
        }
    }

    "Success withDefault producer returns actual value" {
        checkAll { value: Int, defaultValue: Int ->
            Success<Unit, Int>(value).withDefault { defaultValue } shouldBe value
        }
    }

    "Failure withDefault producer returns default value" {
        checkAll { errorValue: Int ->
            Failure<Int, Int>(errorValue).withDefault { error -> 2 * error } shouldBe 2 * errorValue
        }
    }

    "Success can be mapped over" {
        checkAll { value: Int ->
            Success<Unit, Int>(value).map { 2 * it } shouldBe Success(2 * value)
        }
    }

    "Failure can be mapped over" {
        Failure<Unit, Int>(Unit).map { 2 * it } shouldBe Failure(Unit)
    }

    "Success can be error mapped over" {
        checkAll { value: Int ->
            Success<Unit, Int>(value).mapError { "something went wrong" } shouldBe Success<Unit, Int>(value)
        }
    }

    "Failure can be error mapped over" {
        Failure<Unit, Int>(Unit).mapError { "something went wrong" } shouldBe Failure("something went wrong")
    }

    "Success value can be used" {
        checkAll { value: Int ->
            val receiver = UserOfValue()
            val result: Result<Unit, Int> = Success(value)

            result.use(receiver::receive)

            receiver.wasCalled() shouldBe true
            receiver.received shouldNot beNull()
            receiver.received shouldBe value
        }
    }

    "Failure can be used but nothing will be received" {
        val receiver = UserOfValue()
        val result: Result<Unit, Int> = Failure(Unit)

        result.use(receiver::receive)

        receiver.wasCalled() shouldBe false
    }

    "Success can be composed" {
        checkAll { value: Int ->
            val result: Result<Unit, Int> = Success(value)

            val actual = result.andThen(::successfulIncrement)

            actual shouldBe Success(value + 1)
        }
    }

    "Failure can be composed" {
        val result: Result<Unit, Int> = Failure(Unit)

        val actual = result.andThen(::successfulIncrement)

        actual shouldBe Failure(Unit)
    }

    "Success can be composed with Failure" {
        checkAll { value: Int ->
            val result: Result<Unit, Int> = Success(value)

            val actual = result.andThen { Failure<Unit, Int>(Unit)}

            actual shouldBe Failure(Unit)
        }
    }
})

fun successfulIncrement(value: Int): Result<Unit, Int> {
    return Success(value + 1)
}

class UserOfValue {
    var received: Int? = null
    fun receive(value: Int) {
        received = value
    }

    fun wasCalled(): Boolean {
        return received != null
    }
}
