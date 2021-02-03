package com.alliander.example

import com.alliander.example.dice.Dice
import com.alliander.example.dice.toDice

fun main(args: Array<String>) {
    val input = args[0]
    val roll = input
        .toDice()
        .andThen(Dice::roll)
        .use { pips -> println("$input threw $pips")}
        .useError { error -> println("could not roll dice: $error")}
}
