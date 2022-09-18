package com.orhan.plugins

import com.orhan.session.Session
import io.ktor.sessions.*
import io.ktor.application.*
import io.ktor.util.*

fun Application.configureSecurity() {
    install(Sessions) {
        cookie<Session>("SESSION")
    }

    intercept(ApplicationCallPipeline.Features) {
        if(call.sessions.get<Session>() == null) {
            val uniqueId = call.parameters["userId"] ?: "-1"
            call.sessions.set(Session(uniqueId, generateNonce()))
        }
    }

}
