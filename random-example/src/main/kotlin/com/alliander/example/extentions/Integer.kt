package com.alliander.example.extentions

fun Int.modulo(m: Int): Int {
    val result = this % m
    return if (result >= 0) {
        result
    } else {
        result + m
    }
}
