package com.alliander.result

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class Map3Test : StringSpec({
    "map3(success, success, success) is a success" {
        checkAll { l: Int, m: Int, r: Int ->
            val left: Result<String, Int> = Success(l)
            val middle: Result<String, Int> = Success(m)
            val right: Result<String, Int> = Success(r)

            val actual = map3(left, middle, right, ::add)

            actual shouldBe Success(l + m + r)
        }
    }

    "map3(failure, success, success) is a failure" {
        checkAll { m: Int, r: Int ->
            val left: Result<String, Int> = Failure("left is a failure")
            val middle: Result<String, Int> = Success(m)
            val right: Result<String, Int> = Success(r)

            val actual = map3(left, middle, right, ::add)

            actual shouldBe Failure("left is a failure")
        }
    }

    "map3(success, failure, success) is a failure" {
        checkAll { l: Int, r: Int ->
            val left: Result<String, Int> = Success(l)
            val middle: Result<String, Int> = Failure("middle is a failure")
            val right: Result<String, Int> = Success(r)

            val actual = map3(left, middle, right, ::add)

            actual shouldBe Failure("middle is a failure")
        }
    }

    "map3(success, success, failure) is a failure" {
        checkAll { l: Int, m: Int ->
            val left: Result<String, Int> = Success(l)
            val middle: Result<String, Int> = Success(m)
            val right: Result<String, Int> = Failure("right is a failure")

            val actual = map3(left, middle, right, ::add)

            actual shouldBe Failure("right is a failure")
        }
    }

    "map3(failure, failure, success) is a failure" {
        checkAll { r: Int ->
            val left: Result<String, Int> = Failure("left is a failure")
            val middle: Result<String, Int> = Failure("middle is a failure")
            val right: Result<String, Int> = Success(r)

            val actual = map3(left, middle, right, ::add)

            actual shouldBe Failure("left is a failure")
        }
    }

    "map3(failure, success, failure) is a failure" {
        checkAll { m: Int ->
            val left: Result<String, Int> = Failure("left is a failure")
            val middle: Result<String, Int> = Success(m)
            val right: Result<String, Int> = Failure("right is a failure")

            val actual = map3(left, middle, right, ::add)

            actual shouldBe Failure("left is a failure")
        }
    }

    "map3(success, failure, failure) is a failure" {
        checkAll { l: Int ->
            val left: Result<String, Int> = Success(l)
            val middle: Result<String, Int> = Failure("middle is a failure")
            val right: Result<String, Int> = Failure("right is a failure")

            val actual = map3(left, middle, right, ::add)

            actual shouldBe Failure("middle is a failure")
        }
    }


    "map3(failure, failure, failure) is a failure" {
        val left: Result<String, Int> = Failure("left is a failure")
        val middle: Result<String, Int> = Failure("middle is a failure")
        val right: Result<String, Int> = Failure("right is a failure")

        val actual = map3(left, middle, right, ::add)

        actual shouldBe Failure("left is a failure")
    }
})

fun add(l: Int, m: Int, r: Int): Int =
    l + m + r
