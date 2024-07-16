package com.orhan.plugins

import com.orhan.controllers.AppController
import com.orhan.controllers.MainController
import com.orhan.routes.appSocket
import com.orhan.routes.getTesting
import com.orhan.routes.orderSocket
import io.ktor.routing.*
import io.ktor.application.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val roomController by inject<MainController>()
    val appController by inject<AppController>()
    install(Routing) {
        orderSocket(roomController)
        appSocket(appController)
        getTesting()
    }
}
