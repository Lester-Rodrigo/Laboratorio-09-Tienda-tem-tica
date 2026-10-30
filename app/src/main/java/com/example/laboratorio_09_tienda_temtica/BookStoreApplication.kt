package com.example.laboratorio_09_tienda_temtica

import android.app.Application
import com.example.laboratorio_09_tienda_temtica.di.AppContainer

class BookStoreApplication : Application() {
    val container: AppContainer by lazy {
        AppContainer()
    }
}
