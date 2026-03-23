package com.obscuracalc.app

import android.app.Application

class ObscuraCalcApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
