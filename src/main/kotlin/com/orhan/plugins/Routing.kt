package com.orhan.plugins

import com.orhan.controllers.MainController
import com.orhan.routes.getTesting
import com.orhan.routes.orderSocket
import io.ktor.routing.*
import io.ktor.application.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val roomController by inject<MainController>()
    install(Routing) {
        orderSocket(roomController)
        getTesting()
    }
}
