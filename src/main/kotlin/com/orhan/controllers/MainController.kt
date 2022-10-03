package com.orhan.controllers

import com.google.gson.Gson
import com.orhan.calculations.fetchClosePrice
import com.orhan.data.Directive
import com.orhan.utils.DateTimeManager
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.Math.pow
import java.text.DateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

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

                            val a = async { fetchClosePrice(httpClient = httpClient, period = "5m") }
                            val b = async { fetchClosePrice(httpClient = httpClient, period = "15m") }
                            val c = async { fetchClosePrice(httpClient = httpClient, period = "30m") }
                            val d = async { fetchClosePrice(httpClient = httpClient, period = "60m") }
                            val e = async { fetchClosePrice(httpClient = httpClient, period = "90m") }
                            val f = async { fetchClosePrice(httpClient = httpClient, period = "1h") }

                            val x = mapOf(
                                "data" to mapOf(
                                    "5m" to a.await(),
                                    "15m" to b.await(),
                                    "30m" to c.await(),
                                    "60m" to d.await(),
                                    "90m" to e.await(),
                                    "1h" to f.await()
                                ),
                                "time" to DateTimeManager.convertDateObject(
                                    Date().time,
                                    DateTimeManager.timeFormatSecs
                                )
                            )

                            member.socket.send(Frame.Text(x.toString()))

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