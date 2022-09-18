package com.orhan.di

import com.orhan.controllers.MainController
import org.koin.dsl.module

val mainModule = module {
    single {
        MainController()
    }
}