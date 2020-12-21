package com.alliander.result

sealed class Result<Error, Value> {
    fun withDefault(defaultValue: Value): Value {
        return withDefault { defaultValue }
    }

    fun withDefault(producer: (Error) -> Value): Value {
        return when (this) {
            is Success -> data
            is Failure -> producer(error)
        }
    }

    fun orThrowException(exceptionProducer: (Error) -> Exception): Value {
        return when (this) {
            is Success -> data
            is Failure -> throw exceptionProducer(error)
        }
    }

    fun <T> andThen(chain: (Value) -> Result<Error, T>): Result<Error, T> {
        return when (this) {
            is Success -> chain(data)
            is Failure -> Failure(error)
        }
    }

    fun <T> map(transform: (Value) -> T): Result<Error, T> {
        return when (this) {
            is Success -> Success(transform(data))
            is Failure -> Failure(error)
        }
    }

    fun <T> mapError(transform: (Error) -> T): Result<T, Value> {
        return when (this) {
            is Success -> Success(data)
            is Failure -> Failure(transform(error))
        }
    }

    fun use(actOn: (Value) -> Unit): Result<Error, Value> {
        when (this) {
            is Success -> actOn(data)
            is Failure -> Unit
        }
        return this
    }

    fun useError(actOn: (Error) -> Unit): Result<Error, Value> {
        when (this) {
            is Success -> Unit
            is Failure -> actOn(error)
        }
        return this
    }
}

data class Success<Error, Value>(val data: Value) : Result<Error, Value>() {
    override fun toString(): String {
        return "Success(data=$data)"
    }
}

data class Failure<Error, Value>(val error: Error) : Result<Error, Value>() {
    override fun toString(): String {
        return "Failure(error=$error)"
    }
}

/** {@code map2} can be used to map over two results.
 *
 * It accepts two results and transforms their successes into a result.
 *
 * @param left the {@code Result} on the left
 * @param <U> the success content type of the {@code left} parameter
 * @param right the {@code Result} on the right
 * @param <V> the success content type off the {@code right} parameter
 * @param transform a function accepts an argument of type {@code U} and of type {@code V} and transforms it in an object of type {@code W}
 * @param <W> the success content of the returned result
 * @param <E> the common error type of the results involved
 * @return the result of applying {@code transform} to the success values of {@code left} and {@code right}. If {@code left} is a {@code Failure} return that failure. Otherwise if {@code right} is a {@code Failure} return that failure.
 * @see {@code Map2Test.kt}
 */
fun <E, U, V, W> map2(left: Result<E, U>, right: Result<E, V>, transform: (U, V) -> W): Result<E, W> {
    return left.andThen { l -> right.map { r -> transform(l, r)}}
}

/** {@code map3} can be used to map over three results.
 *
 * It accepts three results and transforms their successes into a result.
 *
 * @param left the {@code Result} on the left
 * @param <U> the success content type of the {@code left} parameter
 * @param middle the {@code Result} in the middle
 * @param <V> the success content type of the {@code middle} parameter
 * @param right the {@code Result} on the right
 * @param <W> the success content type off the {@code right} parameter
 * @param transform a function accepts an argument of type {@code U}, of type {@code V} and of type {@code W} and transforms it in an object of type {@code X}
 * @param <X> the success content of the returned result
 * @param <E> the common error type of the results involved
 * @return the result of applying {@code transform} to the success values of {@code left}, {@code middle} and {@code right}. If {@code left} is a {@code Failure} return that failure. Otherwise if {@code middle} is a {@code Failure} return that failure. Otherwise if {@code right} is a {@code Failure} return that failure.
 * @see {@code Map3Test.kt}
 */
fun <E, U, V, W, X> map3(left: Result<E, U>, middle: Result<E, V>, right: Result<E, W>, transform: (U, V, W) -> X): Result<E, X> {
    return left.andThen { l ->
        map2(middle, right) { m, r -> transform(l, m, r) }
    }
}
