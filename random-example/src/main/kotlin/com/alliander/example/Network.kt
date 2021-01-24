package com.alliander.example

import com.alliander.result.Result

interface Network {
    fun fetch(): Result<NetworkError, List<Int>>
}

enum class NetworkError {
    Generic,
    NotOK,
    NotAnInteger,
}
