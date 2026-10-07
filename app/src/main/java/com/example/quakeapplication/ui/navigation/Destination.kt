package com.example.quakeapplication.ui.navigation

import kotlinx.serialization.Serializable

// Type-safe navigation: each screen is a class, and its arguments are its fields
// The compiler checks every navigate() call, unlike hand-written route strings

@Serializable
data object ListDestination

@Serializable
data object MapDestination

// The id travels with the navigation entry and survives process death,
// so the detail screen can rebuild itself after the system kills the app in the background
@Serializable
data class DetailDestination(val earthquakeId: String)