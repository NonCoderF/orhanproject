package com.orhan.controllers

import com.google.gson.Gson
import com.orhan.calculations.fetch
import com.orhan.data.Directive
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.Math.pow
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

                            val arrayList = ArrayList<Deferred<JSONObject?>>()
                            arrayList.apply {
                                add(async { fetch(httpClient = httpClient, period = "5m") })
                                add(async { fetch(httpClient = httpClient, period = "15m") })
                                add(async { fetch(httpClient = httpClient, period = "30m") })
                                add(async { fetch(httpClient = httpClient, period = "60m") })
                                add(async { fetch(httpClient = httpClient, period = "90m") })
                                add(async { fetch(httpClient = httpClient, period = "1h") })
                            }

                            val x = arrayList.awaitAll()

                            member.socket.send(Frame.Text(Gson().toJson(x)))

                            delay(5000)
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