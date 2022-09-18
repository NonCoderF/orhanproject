package com.orhan.di

import com.orhan.controllers.MainController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val mainModule = module {
    single {
        CoroutineScope(Dispatchers.IO)
    }
    single {
        MainController(get())
    }
}