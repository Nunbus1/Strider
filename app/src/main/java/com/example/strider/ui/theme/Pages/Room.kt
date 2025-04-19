package com.example.strider.ui.theme.Pages

data class Room(
    val code: String,
    val hostId: String = "0",
    val hostLaunchGame: Boolean = false,
    val lastPlayerIndex: Int = 0
)