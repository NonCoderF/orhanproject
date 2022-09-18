package com.orhan.routes

import com.orhan.controllers.MemberAlreadyConnectedException
import com.orhan.controllers.MainController
import com.orhan.session.Session
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

fun Route.orderSocket(mainController: MainController) {

    webSocket("/socket") {
        val session = call.sessions.get<Session>()
        if(session == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session."))
            return@webSocket
        }
        try {
            mainController.onJoin(
                userId = session.userId,
                sessionId = session.sessionId,
                socket = this
            )

            incoming.consumeEach { frame ->
                if(frame is Frame.Text) {

                    mainController.sendMessage(
                        senderId = session.userId,
                        message = frame.readText()
                    )

                }
            }
        } catch(e: MemberAlreadyConnectedException) {
            call.respond(HttpStatusCode.Conflict)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mainController.tryDisconnect(session.userId)
        }
    }

}

fun Route.getTesting() {
    get("/") {
        call.respond("I'm alive!")
    }
}