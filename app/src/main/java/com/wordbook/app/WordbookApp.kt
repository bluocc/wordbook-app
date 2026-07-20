package com.wordbook.app

import android.app.Application
import com.wordbook.app.data.database.AppDatabase

class WordbookApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: WordbookApp
            private set
    }
}
