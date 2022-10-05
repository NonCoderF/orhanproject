package com.orhan.parser

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.orhan.utils.DateTimeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL

data class Price(
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val time: String,
    val interval: String
)

suspend fun parsePrice(): JsonObject {
    val url = URL("http://stock-rock-007.herokuapp.com?ticker=ADANIPORTS.NS&interval=1m&period=1d")

    var responseString = ""

    with(withContext(Dispatchers.IO) {
        url.openConnection()
    } as HttpURLConnection) {
        requestMethod = "GET"  // optional default is GET

        inputStream.bufferedReader().use {
            it.lines().forEach { line ->
                responseString = line
            }
        }
    }

    return Klaxon().parseJsonObject(StringReader(responseString))
}

fun parseWindow(json: JsonObject, interval: String = ""): Price {

    fun findIndex(keys: List<String>): Int = when (interval) {
        "1m" -> if (keys.size > 1) 2 else keys.size - 1
        "5m" -> if (keys.size > 6) 6 else keys.size - 1
        "15m" -> if (keys.size > 15) 16 else keys.size - 1
        "60m" -> if (keys.size > 60) 61 else keys.size - 1
        "90m" -> if (keys.size > 90) 91 else keys.size - 1
        "1d" -> keys.size - 1
        else -> 1
    }

    val r = (json["Close"] as JsonObject).keys.toList()
    val closePrice = (json["Close"] as JsonObject).float(r[r.size - 1]) ?: 0F

    val r1 = (json["Open"] as JsonObject).keys.toList()
    val openPrice = (json["Open"] as JsonObject).float(r1[r1.size - findIndex(r1)]) ?: 0F

    val r2 = (json["High"] as JsonObject).keys.toList()
    val highPrice = (json["High"] as JsonObject).float(r2[r2.size - findIndex(r2)]) ?: 0F

    val r3 = (json["Low"] as JsonObject).keys.toList()
    val lowPrice = (json["Low"] as JsonObject).float(r3[r3.size - findIndex(r3)]) ?: 0F

    DateTimeManager.convertDateObject(r1[r1.size - findIndex(r1)].toLong(), DateTimeManager.timeFormat)

    val time0 = DateTimeManager.convertDateObject(r1[r1.size - findIndex(r1)].toLong(), DateTimeManager.timeFormat)
    val time1 = DateTimeManager.convertDateObject(r1[r1.size - 1].toLong(), DateTimeManager.timeFormat)

    println("$interval, Index is : ${r1[r1.size - findIndex(r1)].toLong()}, (" + time0 + " - " + time1 + ")" + ", Open : " + openPrice)

    return Price(
        open = openPrice,
        high = highPrice,
        low = lowPrice,
        close = closePrice,
        time = "($time0 - $time1)",
        interval = interval
    )
}