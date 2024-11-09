package com.fakhrirasyids.sample.di

import android.app.NotificationManager
import com.fakhrirasyids.leech.Leech
import com.fakhrirasyids.sample.R
import com.fakhrirasyids.sample.ui.view.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dependencyModule = module {
    single {
        Leech.getInstance(
            androidContext(),
            R.drawable.ic_launcher_foreground,
            NotificationManager.IMPORTANCE_DEFAULT
        )
    }
}

val viewModelModule = module {
    viewModel { HomeViewModel(get<Leech>()) }
}