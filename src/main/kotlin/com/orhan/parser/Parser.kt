package com.orhan.parser

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.orhan.utils.DateTimeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL

data class Price(
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Long,
    val time: String,
    val interval: String
)

suspend fun parsePrice(ticker : String): JsonObject {
    val url = URL("http://0.0.0.0:5001?ticker=$ticker&interval=1m&period=1d")

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

    val closeKeys = (json["Close"] as JsonObject).map.keys.toList()
    val openKeys = (json["Open"] as JsonObject).map.keys.toList()
    val highKeys = (json["High"] as JsonObject).map.keys.toList()
    val lowKeys = (json["Low"] as JsonObject).map.keys.toList()
    val volumeKeys = (json["Volume"] as JsonObject).map.keys.toList()

    val closePrice = (json["Close"] as JsonObject).double(closeKeys.last())?.toFloat() ?: 0F
    val openPrice = (json["Open"] as JsonObject).double(openKeys[openKeys.size - findIndex(openKeys)])?.toFloat() ?: 0F
    val highPrice = (json["High"] as JsonObject).double(highKeys[highKeys.size - findIndex(highKeys)])?.toFloat() ?: 0F
    val lowPrice = (json["Low"] as JsonObject).double(lowKeys[lowKeys.size - findIndex(lowKeys)])?.toFloat() ?: 0F
    val volume = (json["Volume"] as JsonObject).long(volumeKeys[volumeKeys.size - findIndex(volumeKeys)]) ?: 0L

    val time0 = DateTimeManager.convertDateObject(openKeys[openKeys.size - findIndex(openKeys)].toLong(), DateTimeManager.timeFormat)
    val time1 = DateTimeManager.convertDateObject(openKeys.last().toLong(), DateTimeManager.timeFormat)

    return Price(
        open = openPrice,
        high = highPrice,
        low = lowPrice,
        close = closePrice,
        volume = volume,
        time = "($time0 - $time1)",
        interval = interval
    )
}