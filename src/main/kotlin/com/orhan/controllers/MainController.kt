package com.orhan.controllers

import com.google.gson.Gson
import com.orhan.calculations.calculateTrend
import com.orhan.data.Directive
import com.orhan.parser.parsePrice
import com.orhan.parser.parseWindow
import com.orhan.utils.DateTimeManager
import io.ktor.client.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

class MainController(
    val httpClient: HttpClient
) {

    private val members = ConcurrentHashMap<String, Member>()

    fun onJoin(
        userId: String, sessionId: String, socket: WebSocketSession
    ) {
        if (members.containsKey(userId)) {
            throw MemberAlreadyConnectedException()
        }

        members[userId] = Member(
            userId = userId, sessionId = sessionId, socket = socket
        )
    }

    suspend fun sendMessage(senderId: String, message: String) {

        CoroutineScope(Dispatchers.IO).launch {

            val directive = Gson().fromJson(message, Directive::class.java)

            members.values.forEach { member ->

                if (member.userId in directive.receivers) {

                    while (true) {

                        while (true) {

                            val prices = parsePrice()

                            val day = parseWindow(json = prices, interval = "1d")
                            val last90min = parseWindow(json = prices, interval = "90m")
                            val last60min = parseWindow(json = prices, interval = "60m")
                            val last15min = parseWindow(json = prices, interval = "15m")
                            val last5min = parseWindow(json = prices, interval = "5m")
                            val last1min = parseWindow(json = prices, interval = "1m")

                            val a = JSONObject()

                            a.put("data", JSONObject().apply {
                                put("day      ", calculateTrend(day.also {
                                    a.put("close", it.close)
                                }))
                                put("last90min", calculateTrend(last90min))
                                put("last60min", calculateTrend(last60min))
                                put("last15min", calculateTrend(last15min))
                                put("last5min ", calculateTrend(last5min))
                                put("last1min ", calculateTrend(last1min))
                            })

                            a.put("time", DateTimeManager.convertDateObject(
                                Date().time,
                                DateTimeManager.timeFormatSecs
                            ))

                            member.socket.send(Frame.Text(a.toString(2)))

                            delay(10000)
                        }
                    }

                }
            }
        }
    }


    suspend fun tryDisconnect(username: String) {
        members[username]?.socket?.close()
        if (members.containsKey(username)) {
            members.remove(username)
        }
    }
}