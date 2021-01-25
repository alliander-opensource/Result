package com.alliander.result

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class Map2Test : StringSpec({
    "map2(success, success) is a success" {
        checkAll { l: Int, r: Int ->
            val left: Result<String, Int> = Success(l)
            val right: Result<String, Int> = Success(r)

            val actual = map2(left, right, ::add)

            actual shouldBe Success(l + r)
        }
    }

    "map2(failure, success) is a failure" {
        checkAll { v: Int ->
            val left: Result<String, Int> = Failure("left is a failure")
            val right: Result<String, Int> = Success(v)

            val actual = map2(left, right, ::add)

            actual shouldBe Failure("left is a failure")
        }
    }

    "map2(failure, failure) is a failure" {
        val left: Result<String, Int> = Failure("left is a failure")
        val right: Result<String, Int> = Failure("right is a failure")

        val actual = map2(left, right, ::add)

        actual shouldBe Failure("left is a failure")
    }
})

fun add(left: Int, right: Int): Int =
    left + right

