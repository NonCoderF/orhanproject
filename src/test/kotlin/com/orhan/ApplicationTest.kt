package com.orhan

import com.orhan.di.mainModule
import kotlin.test.*
import io.ktor.server.testing.*
import com.orhan.plugins.*
import org.junit.Before
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin

class ApplicationTest {
    @Before
    fun before() {
        stopKoin() // to remove 'A Koin Application has already been started'
        startKoin {
            modules(mainModule)
        }
    }
    @Test
    fun testRoot() {
        withTestApplication({ configureRouting() }) {
            handleWebSocket(uri = "ws://0.0.0.0:9000/order?userId=1", setup = {
                this.setBody("{" +
                        "    orderId : 1," +
                        "    statusCode : 1001," +
                        "    receivers : [" +
                        "        -1" +
                        "    ]" +
                        "}")
            }).apply {

            }
        }
    }
}