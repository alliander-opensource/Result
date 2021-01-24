package com.alliander.example.sources

import com.alliander.example.Source
import com.alliander.example.SourceError
import com.alliander.result.Result

data class EvenSource(val source: Source): Source {
    override fun integer(): Result<SourceError, Int> {
        return source.integer()
                .map { 2 * it }
    }
}
