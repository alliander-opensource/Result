package com.alliander.example

import com.alliander.example.networks.RandomOrg
import com.alliander.example.sources.NetworkSource
import com.alliander.result.map2
import java.net.http.HttpClient

fun main(args: Array<String>) {
    val client = HttpClient.newHttpClient()
    val source = NetworkSource(RandomOrg(client))

    val first = source.integer()
    val second = source.integer()

    val sum = map2(first, second) { l, r -> l + r }

    println("$first + $second = $sum")
}
