package com.example.quakeapplication

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// This annotation tells Hilt: build the app-wide container of shared objects here
// The container lives exactly as long as the app process lives
// It must extend Application (not an Activity) and be registered in AndroidManifest.xml via android:name
@HiltAndroidApp
class QuakeApplication : Application()
