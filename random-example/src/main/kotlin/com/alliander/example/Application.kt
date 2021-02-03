package com.alliander.example

import com.alliander.example.dice.Dice
import com.alliander.example.dice.toDice

fun main(args: Array<String>) {
    val input = args[0]
    val roll = input
        .toDice()
        .andThen(Dice::roll)
        .withDefault(0)

    println("$input threw $roll")
}
