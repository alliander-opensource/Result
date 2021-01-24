package com.alliander.example

import com.alliander.result.Result

interface Source {
    fun integer(): Result<SourceError, Int>
}

enum class SourceError {
    Generic,
    Empty,
}
