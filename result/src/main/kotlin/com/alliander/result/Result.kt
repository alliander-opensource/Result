package com.alliander.result

/**
 * Return type of a computation that could fail.
 *
 * [Result] has two subclasses, [Success] and [Failure]. [Success] is used for successful computations.
 * [Failure] is used for computations that have failed.
 *
 * [Result] is dependent on two generic parameters; `Error` and `Value`. `Error` is a type that distinguishes between
 * the kind of failures that could occur. `Value` is the type of successful computations.
 *
 * Although [Result] is a sealed class and can be safely switched over, the idiomatic use is to build up a chain of
 * results with the methods on this class. For an extended example check out this projects
 * [wiki](https://github.com/alliander-opensource/Result/wiki).
 *
 * @param Error the type that indicates which errors can occur.
 * @param Value the type of a successful computation
 * @see Success
 * @see Failure
 */
sealed class Result<Error, Value> {
    /**
     * Unwrap the result by providing a default value.
     *
     * If a computation was a [Success], return that data. Otherwise return the provided default value.
     *
     * @param defaultValue The value in case of a [Failure].
     * @return The data of a [Success], `defaultValue` otherwise.
     */
    fun withDefault(defaultValue: Value): Value {
        return withDefault { defaultValue }
    }

    /**
     * Unwrap the result by providing a default value that can depend on the kind of failure.
     *
     * if a computation was a [Success], return that data. Otherwise calculate a default value, depending on the
     * failure.
     *
     * @param producer Provides the default value in case of a [Failure].
     * @return The data of a [Success]. The application of `producer` to the failure otherwise.
     *
     */
    fun withDefault(producer: (Error) -> Value): Value {
        return when (this) {
            is Success -> data
            is Failure -> producer(error)
        }
    }

    /**
     * Unwrap the result by throwing an [Exception] that can depend on the kind of failure
     *
     * if a computation was a [Success], return that data. Otherwise calculate and throw an exception, depending on the
     * failure.
     *
     * @param exceptionProducer Provides the exception to throw in case of a [Failure].
     * @return The data of a [Success]. Throws an exception in case of a failure.
     * @throws Exception
     */
    fun orThrowException(exceptionProducer: (Error) -> Exception): Value {
        return when (this) {
            is Success -> data
            is Failure -> throw exceptionProducer(error)
        }
    }

    /**
     * Chain two computations together
     *
     * If in a computation chain the result of one computation is needed as input for another computation, `andThen` is
     * there to chain the computations together.
     *
     * Assume we have a result of fetching an user from a repository. We would like to send this user a notification,
     * which in it self could fail as well.
     *
     * With the following definitions.
     *
     * ```kotlin
     * data class User(val username: String, contactInfo: ContactInformation)
     *
     * interface UserRepository {
     *     fun fetchByUsername(username: String)
     * }
     *
     * fun sendNotification(user: User): Result<NotificationProblem, Record> {
     *     // implementation omitted
     * }
     *
     * enum class NotificationProblem {
     *     CouldNotFetchUser,
     *     CouldNotSendNotification,
     * }
     * ```
     *
     * Sending a notification to a `User` amounts to
     *
     * ```kotlin
     * val user: Result<NotificationProblem, User> = repository.fetchByUsername('dvberkel')
     * val record: Result<NotificationProblem, Record> = user.andThen(::sendNotification)
     * ```
     *
     * Or without intermediate variables
     *
     * ```kotlin
     * val record = repository.fetchByUsername('dvberkel').andThen(::sendNotification)
     * ```
     */
    fun <T> andThen(chain: (Value) -> Result<Error, T>): Result<Error, T> {
        return when (this) {
            is Success -> chain(data)
            is Failure -> Failure(error)
        }
    }

    /**
     * Chain two computations together for error recovery.
     *
     * If a computation was a [Failure], `recover` can chain the error data into an error recovery computation.
     * Otherwise keep the [Success].
     *
     * @param transform Provides the transformed error
     * @return The transformed error of a [Failure]. Keeps the data otherwise.
     */
    fun <T> andThenError(transform: (Error) -> Result<T, Value>): Result<T, Value> {
        return when (this) {
            is Success -> Success(data)
            is Failure -> transform(error)
        }
    }

    /**
     * Transform a successful result.
     *
     * If a computation was a [Success], transform the data. Otherwise keep the [Failure].
     *
     * @param transform Provides the transformed data
     * @return The transformed data of a [Success]. Keeps the failure otherwise.
     */
    fun <T> map(transform: (Value) -> T): Result<Error, T> {
        return when (this) {
            is Success -> Success(transform(data))
            is Failure -> Failure(error)
        }
    }

    /**
     * Transform a failed result.
     *
     * If a computation was a [Failure], transform the error. Otherwise keep the [Success].
     *
     * @param transform Provides the transformed error
     * @return The data of a [Success]. The transformed error otherwise.
     */
    fun <T> mapError(transform: (Error) -> T): Result<T, Value> {
        return when (this) {
            is Success -> Success(data)
            is Failure -> Failure(transform(error))
        }
    }

    /**
     * Use the data of a successful result.
     *
     * Use the data of a successful result in the middle of a computation chain.
     *
     * @param actOn the action to be executed with the value.
     * @return the original result.
     */
    fun use(actOn: (Value) -> Unit): Result<Error, Value> {
        when (this) {
            is Success -> actOn(data)
            is Failure -> Unit
        }
        return this
    }

    /**
     * Use the error of a failed result.
     *
     * Use the error of a failed result in the middle of a computation chain.
     *
     * @param actOn the action to be executed with the error.
     * @return the original result.
     */
    fun useError(actOn: (Error) -> Unit): Result<Error, Value> {
        when (this) {
            is Success -> Unit
            is Failure -> actOn(error)
        }
        return this
    }
}

/**
 * A successful result of a computation.
 *
 * @param data the value returned by the computation.
 * @see [Result]
 */
data class Success<Error, Value>(val data: Value) : Result<Error, Value>() {
    override fun toString(): String {
        return "Success(data=$data)"
    }
}

/**
 * A failed result of a computation
 *
 * @param error the problem that occurred
 * @see [Result]
 */
data class Failure<Error, Value>(val error: Error) : Result<Error, Value>() {
    override fun toString(): String {
        return "Failure(error=$error)"
    }
}

/**
 * Map over two results.
 *
 * Accepts two results and transform their successes into a result.
 *
 * @param left the [Result] on the left.
 * @param U the value type of the `left` result.
 * @param right the [Result] on the right.
 * @param V the value type of the `right` result.
 * @param transform a transforms of two arguments of type `U` and `V` to an instance of type `W`.
 * @param W the value type of the returned result.
 * @param E the common error type of the results involved.
 * @return the result of applying `transform` to the success values of `left` and `right`. If `left` is a [Failure]
 * return that failure. Otherwise if `right` is a [Failure] return that failure.
 * @see [Map2Test.kt](https://github.com/alliander-opensource/Result/blob/master/result/src/test/kotlin/com/alliander/result/Map2Test.kt)
 */
fun <E, U, V, W> map2(left: Result<E, U>, right: Result<E, V>, transform: (U, V) -> W): Result<E, W> {
    return left.andThen { l -> right.map { r -> transform(l, r)}}
}

/**
 * Map over three results.
 *
 * Accepts three results and transform their successes into a result.
 *
 * @param left the [Result] on the left.
 * @param U the value type of the `left` result.
 * @param middle the [Result] in the middle.
 * @param V the value type of the `middle` result.
 * @param right the [Result] on the right.
 * @param W the value type of the `right` result.
 * @param transform a transforms of three arguments of type `U`, `V` and `W` to an instance of type `X`.
 * @param X the success content of the returned result
 * @param E the common error type of the results involved
 * @return the result of applying `transform` to the success values of `left`, `middle` and `right`. If `left` is a
 * [Failure] return that failure. Otherwise if `middle` is a [Failure] return that failure. Otherwise if `right` is a
 * [Failure] return that failure.
 * @see [Map3Test.kt](https://github.com/alliander-opensource/Result/blob/master/result/src/test/kotlin/com/alliander/result/Map3Test.kt)
 */
fun <E, U, V, W, X> map3(left: Result<E, U>, middle: Result<E, V>, right: Result<E, W>, transform: (U, V, W) -> X): Result<E, X> {
    return left.andThen { l ->
        map2(middle, right) { m, r -> transform(l, m, r) }
    }
}
