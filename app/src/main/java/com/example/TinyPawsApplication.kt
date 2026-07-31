package com.example

import android.app.Application
import com.example.ui.TranslationManager

class TinyPawsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TranslationManager.load(this)
    }
}
