package com.orhan.controllers

import com.google.gson.Gson
import com.orhan.calculations.Projectile
import com.orhan.calculations.getPriceProjectileString
import com.orhan.data.Directive
import com.orhan.memory.Memory
import com.orhan.parser.parsePrice
import com.orhan.parser.parseWindow
import io.ktor.client.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

class MainController(
    val httpClient: HttpClient,
    val memory: Memory
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

            while (members.isNotEmpty()){

                members.values.forEach { member ->

                    if(directive.receivers.contains(member.userId)){
                        val prices = parsePrice(ticker = directive.ticker)

                        val day = parseWindow(json = prices, interval = "1d")
                        val last90min = parseWindow(json = prices, interval = "90m")
                        val last60min = parseWindow(json = prices, interval = "60m")
                        val last15min = parseWindow(json = prices, interval = "15m")
                        val last5min = parseWindow(json = prices, interval = "5m")
                        val last1min = parseWindow(json = prices, interval = "1m")

                        val pricesString = getPriceProjectileString(
                            listOf(day, last90min, last60min, last15min, last5min, last1min)
                        )

                        val projectile = Projectile(pricesString)

                        val voter = memory.execute(
                            day, last90min, last60min,
                            last15min, last5min, last1min
                        )

                        val xString = "Projectiles : " +
                                projectile.toString() + " >> " +
                                day.close + " : " +
                                memory.getVoterStats(voter)

                        member.socket.send(Frame.Text(xString))
                    }
                }

                delay(1 * 1000)
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