package com.orhan.controllers

import com.google.gson.Gson
import com.orhan.data.*
import io.ktor.http.cio.websocket.*
import java.util.concurrent.ConcurrentHashMap
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainController() {

    private val members = ConcurrentHashMap<String, Member>()
    val responderCoroutine = CoroutineScope(Dispatchers.IO)

    fun onJoin(
        userId: String,
        sessionId: String,
        socket: WebSocketSession
    ) {
        if (members.containsKey(userId)) {
            throw MemberAlreadyConnectedException()
        }

        members[userId] = Member(
            userId = userId,
            sessionId = sessionId,
            socket = socket
        )
    }

    suspend fun sendMessage(senderId: String, message: String) {


        val directive = Gson().fromJson(message, Directive::class.java)

        members.values.forEach { member ->

            if (member.userId in directive.receivers) {

                responderCoroutine.launch {
                    while (true){
                        val response: HttpResponse = HttpClient(CIO).get(
                            "http://stock-rock-007.herokuapp.com?ticker=SBIN.NS?interval=1d&period=1m"
                        )

                        val responseString = response.content.readUTF8Line(response.content.availableForRead).toString()

                        val json = JSONObject(responseString)

                        val responseJSON= JSONObject()
                        responseJSON.put("timeStamp", System.currentTimeMillis())
                        responseJSON.put("price", json.getJSONObject("Close"))

                        member.socket.send(Frame.Text(responseJSON.toString()))
                        delay(1000)
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