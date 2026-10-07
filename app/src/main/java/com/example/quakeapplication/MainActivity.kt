package com.example.quakeapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.quakeapplication.ui.navigation.QuakeApp
import com.example.quakeapplication.ui.theme.QuakeApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

// This annotation lets Hilt hand objects to this activity and to the screens inside it
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuakeApplicationTheme {
                QuakeApp()
            }
        }
    }
}