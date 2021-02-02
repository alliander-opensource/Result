package com.alliander.example.generators

import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.arbitrary

fun positive(low: Int): Arb<Int> {
    return arbitrary { rs: RandomSource ->
        var n = rs.random.nextInt()
        if (n < 0) {
            n *= -1
        }
        low + n
    }
}

fun positive(low: Int, high: Int): Arb<Int> {
    return arbitrary { rs: RandomSource ->
        rs.random.nextInt(low, high)
    }
}

fun integers(): Arb<Int> {
    return arbitrary { rs: RandomSource -> rs.random.nextInt() }
}
