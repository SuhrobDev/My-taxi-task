package com.example.mytaxihackathon.di

import androidx.room.Room
import com.example.mytaxihackathon.data.local.database.AppDatabase
import com.example.mytaxihackathon.data.local.database.LOCATION_DATABASE_NAME
import com.example.mytaxihackathon.data.repository.MainRepositoryImpl
import com.example.mytaxihackathon.domain.repository.MainRepository
import com.example.mytaxihackathon.presentation.ui.main.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Provide AppDatabase
    single { Room.databaseBuilder(get(), AppDatabase::class.java, LOCATION_DATABASE_NAME).build() }

    // Provide LocationDao
    single { get<AppDatabase>().getLocationDao() }

    // Provide MainRepository
    single<MainRepository> {
        MainRepositoryImpl(get())
    }

    // Provide MainViewModel
    viewModel { MainViewModel(get()) }

    // Provide MapFragment
//    fragment { MapFragment() }
}