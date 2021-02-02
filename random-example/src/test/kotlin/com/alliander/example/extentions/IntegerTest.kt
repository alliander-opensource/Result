package com.alliander.example.extentions

import com.alliander.example.generators.integers
import com.alliander.example.generators.positive
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.property.checkAll

class IntegerTest: StringSpec({
    "modulo should be positive" {
        checkAll(integers(), positive(2)) { n, modulus ->
            (n.modulo(modulus)) shouldBeGreaterThanOrEqual 0
        }
    }
})
