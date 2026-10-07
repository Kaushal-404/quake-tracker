package com.example.quakeapplication.ui.common

import com.example.quakeapplication.domain.model.UserLocation

// What the screen knows about the user's location; a UI concern, so it lives in the UI layer
sealed interface LocationState {
    // Still finding out
    data object Checking : LocationState
    // Not allowed yet: the screen offers a button to ask
    data object PermissionNeeded : LocationState
    // The user said no, or chose "Not now": we stop asking for this session
    data object Declined : LocationState
    // Allowed, but no position could be found, for example location is switched off
    data object Unavailable : LocationState
    data class Available(val location: UserLocation) : LocationState
}

// The position if we have one, otherwise null
val LocationState.locationOrNull: UserLocation?
    get() = (this as? LocationState.Available)?.location
