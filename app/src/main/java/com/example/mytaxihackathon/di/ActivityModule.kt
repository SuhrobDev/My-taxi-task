package com.example.mytaxihackathon.di

import com.example.mytaxihackathon.presentation.ui.main.MainActivity
import org.koin.core.qualifier.named
import org.koin.dsl.module

val activityModule = module {
    scope<MainActivity> {
        scoped(qualifier = named("hello")) { "Hello" }
        scoped(qualifier = named("bye")) { "Bye" }
    }
}