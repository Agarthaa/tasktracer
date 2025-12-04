package com.example.tasktracker

import android.app.Application

class TaskTrackerApp : Application() {

    companion object {
        lateinit var instance: TaskTrackerApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}