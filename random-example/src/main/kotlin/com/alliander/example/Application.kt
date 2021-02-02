package com.alliander.example

import com.alliander.example.dice.Dice
import com.alliander.example.dice.toDice
import com.alliander.example.networks.RandomOrg
import com.alliander.example.sources.NetworkSource
import com.alliander.result.map2
import java.net.http.HttpClient

fun main(args: Array<String>) {
    val input = args[0]
    val roll = input
        .toDice()
        .andThen(Dice::roll)
        .withDefault(0)

    println("$input threw $roll")
}
