package com.alliander.result

inline fun <reified T> T?.toResult(): Result<Unit, T> {
    return if (this != null) {
        Success(this)
    } else {
        Failure(Unit)
    }
}
