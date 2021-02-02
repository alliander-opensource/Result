package example

import com.alliander.result.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.*
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import io.kotest.property.forAll
import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom
import java.util.Random
import kotlin.RuntimeException


interface Source {
    fun random(): Result<Problem, Int>
}

enum class Problem {
    Connection,
    Timeout,
    Overflow
}

data class ProblemOccurredException(val problem: Problem) : RuntimeException()

data class TimestampedProblem(val problem: Problem, val timestamp: LocalDateTime = LocalDateTime.now())

class Receiver<T>(var received: Boolean = false, var value: T? = null) {
    fun receive(receivedValue: T) {
        received = true
        value = receivedValue
    }
}

data class Always(val number: Int) : Source {
    override fun random(): Result<Problem, Int> {
        return Success(number)
    }
}

data class Sometimes(
    val threshold: Double,
    val number: Int,
    val random: Random? = ThreadLocalRandom.current()
) : Source {
    private val randomProblems = listOf(Problem.Timeout, Problem.Overflow)

    override fun random(): Result<Problem, Int> {
        return if (random != null) {
            val p = random.nextDouble()
            if (p < threshold) {
                Success(number)
            } else {
                val randomProblem = randomProblems[random.nextInt(randomProblems.size)]
                Failure(randomProblem)
            }
        } else {
            Failure(Problem.Connection)
        }
    }
}

class Example : StringSpec({
    "creation of a success" {
        Success<Problem, Int>(37)
    }

    "creation of a failure" {
        Failure<Problem, Int>(Problem.Timeout)
    }

    "withDefault of a success" {
        val result: Result<Problem, Int> = Success(37)

        result.withDefault(42) shouldBe 37
    }

    "withDefault of a failure" {
        val result: Result<Problem, Int> = Failure(Problem.Connection)

        result.withDefault(42) shouldBe 42
    }

    "withDefault with producer of a success" {
        val result: Result<Problem, Int> = Success(37)

        result.withDefault { problem ->
            when (problem) {
                Problem.Connection -> -1
                Problem.Timeout -> -2
                Problem.Overflow -> -4
            }
        } shouldBe 37
    }

    "withDefault with producer of a failure" {
        val result: Result<Problem, Int> = Failure(Problem.Overflow)

        result.withDefault { problem ->
            when (problem) {
                Problem.Connection -> -1
                Problem.Timeout -> -2
                Problem.Overflow -> -4
            }
        } shouldBe -4
    }

    "orThrowException of a success" {
        val result: Result<Problem, Int> = Success(37)

        result.orThrowException(::ProblemOccurredException) shouldBe 37
    }

    "orThrowException of a failure" {
        val result: Result<Problem, Int> = Failure(Problem.Overflow)

        shouldThrow<ProblemOccurredException> {
            result.orThrowException(::ProblemOccurredException)
        }
    }

    "map of a success" {
        val result: Result<Problem, Int> = Success(37)

        result.map { 2 * it } shouldBe Success(74)
    }

    "map of a failure" {
        val result: Result<Problem, Int> = Failure(Problem.Timeout)

        result.map { 2 * it } shouldBe Failure(Problem.Timeout)
    }

    "mapError of a success" {
        val result: Result<Problem, Int> = Success(37)
        val timestamp = LocalDateTime.now()

        result.mapError { problem -> TimestampedProblem(problem, timestamp) } shouldBe Success(37)
    }

    "mapError of a failure" {
        val result: Result<Problem, Int> = Failure(Problem.Timeout)
        val timestamp = LocalDateTime.now()

        (result.mapError { problem -> TimestampedProblem(problem, timestamp) }
                shouldBe Failure(TimestampedProblem(Problem.Timeout, timestamp)))
    }

    "andThen of a success with a success" {
        val result: Result<Problem, Int> = Success(37)

        result.andThen { n -> Success(n + 1) } shouldBe Success(38)
    }

    "andThen of a success with a failure" {
        val result: Result<Problem, Int> = Success(37)

        result.andThen { Failure<Problem, Int>(Problem.Timeout) } shouldBe Failure(Problem.Timeout)
    }

    "andThen of a failure with a success" {
        val result: Result<Problem, Int> = Failure(Problem.Connection)

        result.andThen { n -> Success(n + 1) } shouldBe Failure(Problem.Connection)
    }

    "andThen of a failure with a failure" {
        val result: Result<Problem, Int> = Failure(Problem.Connection)

        result.andThen { Failure<Problem, Int>(Problem.Timeout) } shouldBe Failure(Problem.Connection)
    }

    "andThenError of a success with a success" {
        val result: Result<Problem, Int> = Success(37)

        result.andThenError { Success<Problem, Int>(51) } shouldBe Success(37)
    }

    "andThenError of a success with a failure" {
        val result: Result<Problem, Int> = Success(37)

        result.andThenError { Failure<Problem, Int>(Problem.Timeout) } shouldBe Success(37)
    }

    "andThenError of a failure with a success" {
        val result: Result<Problem, Int> = Failure(Problem.Connection)

        result.andThenError { Success<Problem, Int>(51) } shouldBe Success(51)
    }

    "andThenError of a failure with a failure" {
        val result: Result<Problem, Int> = Failure(Problem.Connection)

        result.andThenError { Failure<Problem, Int>(Problem.Timeout) } shouldBe Failure(Problem.Timeout)
    }

    "use of a success" {
        checkAll<Int> { n ->
            val result: Result<Problem, Int> = Success(n)
            val receiver = Receiver<Int>()

            result.use(receiver::receive)

            receiver.received shouldBe true
            receiver.value shouldBe n
        }
    }

    "use of a failure" {
        checkAll(problems) { problem ->
            val result: Result<Problem, Int> = Failure(problem)
            val receiver = Receiver<Int>()

            result.use(receiver::receive)

            receiver.received shouldBe false
        }
    }

    "useError of a success" {
        checkAll<Int> { n ->
            val result: Result<Problem, Int> = Success(n)
            val receiver = Receiver<Problem>()

            result.useError(receiver::receive)

            receiver.received shouldBe false
        }
    }

    "useError of a failure" {
        checkAll(problems) { problem ->
            val result: Result<Problem, Int> = Failure(problem)
            val receiver = Receiver<Problem>()

            result.useError(receiver::receive)

            receiver.received shouldBe true
            receiver.value shouldBe problem
        }
    }

    "complete" {
        forAll<Int, Int> { first, second ->
            val number = Always(first).random()
                .map { m -> 2 * m }
                .andThen { m -> Sometimes(0.9, second).random().map { n -> Pair(m, n) } }
                .map { (m, n) -> m + n }
                .withDefault(0)

            number == 0 || number == 2 * first + second
        }
    }
})

val problems = listOf(*Problem.values()).exhaustive()
