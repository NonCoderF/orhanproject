package com.orhan.calculations

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.json.JSONObject

suspend fun fetchClosePrice(
    httpClient: HttpClient,
    period: String = "5m"
): String {
    val response: HttpResponse = httpClient.get(
        "http://stock-rock-007.herokuapp.com?ticker=ADANIPORTS.NS?interval=1d&period=${period}"
    )

    val responseString = response.content.readUTF8Line(response.content.availableForRead).toString()

    val json = JSONObject(responseString)

    val r = json.getJSONObject("Close").names()
    val closePrice = json.getJSONObject("Close").getFloat("${r[r.length() - 1]}")

    val r1 = json.getJSONObject("Open").names()
    val openPrice = json.getJSONObject("Open").getFloat("${r1[r1.length() - 1]}")

    val r2 = json.getJSONObject("High").names()
    val highPrice = json.getJSONObject("High").getFloat("${r2[r2.length() - 1]}")

    val r3 = json.getJSONObject("Low").names()
    val lowPrice = json.getJSONObject("Low").getFloat("${r3[r3.length() - 1]}")

    return calculateTrend(openPrice, highPrice, lowPrice, closePrice)
}


suspend fun calculateTrend(
    open : Float,
    high : Float,
    low : Float,
    close : Float
): String{
    return if (close > open){
        "uptrend"
    } else if ( close == open) {
        "consolidation"
    } else "downtrend"

}