package com.orhan.di

import com.orhan.controllers.AppController
import com.orhan.controllers.MainController
import com.orhan.memory.AppMemory
import com.orhan.memory.Memory
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import org.koin.dsl.module

val mainModule = module {
    single {
        HttpClient(CIO)
    }
    single {
        MainController(get(), Memory())
    }
    single {
        AppController(get(), AppMemory())
    }
}