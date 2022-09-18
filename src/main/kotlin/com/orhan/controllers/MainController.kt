package com.orhan.controllers

import com.google.gson.Gson
import com.orhan.data.Directive
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import org.json.JSONObject
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


        val directive = Gson().fromJson(message, Directive::class.java)

        members.values.forEach { member ->

            if (member.userId in directive.receivers) {

                while (true) {

                    while (true) {

                        val response: HttpResponse = httpClient.get(
                            "http://stock-rock-007.herokuapp.com?ticker=SBIN.NS?interval=1d&period=1m"
                        )

                        val responseString = response.content.readUTF8Line(response.content.availableForRead).toString()

                        val json = JSONObject(responseString)

                        val responseJSON = JSONObject().apply {
                            put("timeStamp", System.currentTimeMillis())
                            put("price", JSONObject(responseString).getJSONObject("Close"))
                        }
                        responseJSON.put("timeStamp", System.currentTimeMillis())
                        responseJSON.put("price", json.getJSONObject("Close"))

                        member.socket.send(Frame.Text(responseJSON.toString()))

                        delay(500)
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