package com.example.vacationventurepe.application

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore

class RoboApplication:Application() {
    val viewModelProvider: ViewModelProvider by lazy {
        ViewModelProvider(
            ViewModelStore(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(this)
        )
    }
}