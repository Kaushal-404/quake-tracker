package com.example.quakeapplication.domain.model

enum class SortOrder {
    NEWEST,
    STRONGEST,
    // Only meaningful when we know where the user is; otherwise the list keeps the newest-first order
    NEAREST,
}