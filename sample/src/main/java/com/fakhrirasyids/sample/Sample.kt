package com.fakhrirasyids.sample

import android.app.Application
import com.fakhrirasyids.sample.di.dependencyModule
import com.fakhrirasyids.sample.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Sample : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@Sample)
            modules(
                dependencyModule,
                viewModelModule
            )
        }
    }
}