package com.orhan.calculations

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.json.JSONObject

suspend fun fetch(
    httpClient: HttpClient,
    period: String = "5m"
): JSONObject? {
    val response: HttpResponse = httpClient.get(
        "http://stock-rock-007.herokuapp.com?ticker=SBIN.NS?interval=1d&period=${period}"
    )

    val responseString = response.content.readUTF8Line(response.content.availableForRead).toString()

    val json = JSONObject(responseString)

    val responseJSON = JSONObject().apply {
        put("timeStamp", System.currentTimeMillis())
        put("price", JSONObject(responseString).getJSONObject("Close"))
    }
    responseJSON.put("timeStamp", System.currentTimeMillis())
    responseJSON.put("price", json.getJSONObject("Close"))
    return json.getJSONObject("Close")
}