package com.alliander.result

/**
 * Turn a nullable type into a *Result*.
 *
 * For a nullable type `T?`, [toResult] returns a [Failure] when the receiver is `null` and [Success] otherwise.
 *
 * @param T the type of the receiver.
 * @receiver an element of type T?.
 * @return the corresponding result for the receiver.
 */
inline fun <reified T> T?.toResult(): Result<Unit, T> {
    return if (this != null) {
        Success(this)
    } else {
        Failure(Unit)
    }
}
