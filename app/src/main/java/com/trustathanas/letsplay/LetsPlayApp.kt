package com.trustathanas.letsplay

import android.app.Application
import android.content.Context
import android.hardware.SensorManager
import com.trustathanas.letsplay.repositories.TiltRepository
import com.trustathanas.letsplay.viewModels.TiltViewModel
import org.koin.android.ext.android.startKoin
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import java.util.concurrent.Executors

class LetsPlayApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(applicationContextModule, repositoryModule, viewModelModule))
    }

    private val applicationContextModule = module {
        single(name = "context") { applicationContext }
        single { Executors.newCachedThreadPool() }
    }

    private val viewModelModule = module {
        viewModel { TiltViewModel(get()) }
    }

    private val repositoryModule = module {
        single { TiltRepository(get("context"), get()) }
    }

}