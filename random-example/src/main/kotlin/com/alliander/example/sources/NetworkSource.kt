package com.alliander.example.sources

import com.alliander.example.*
import com.alliander.result.*

data class NetworkSource(private val network: Network, private val initialBatch: Collection<Int>): Source {
    constructor(network: Network): this(network, emptyList())

    private val batch = ArrayDeque(initialBatch)

    override fun integer(): Result<SourceError, Int> {
        if (batch.isEmpty()) {
            network.fetch()
                    .use(::fillBatch)
        }

        return batch.removeFirstOrNull().toResult().mapError { SourceError.Empty }
    }

    private fun fillBatch(fresh: Collection<Int>) {
        batch.addAll(fresh)
    }
}
