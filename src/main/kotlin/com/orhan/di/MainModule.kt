package com.orhan.di

import com.orhan.controllers.MainController
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val mainModule = module {
    single {
        HttpClient(CIO)
    }
    single {
        MainController(get())
    }
}