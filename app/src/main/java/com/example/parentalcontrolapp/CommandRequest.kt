package com.example.parentalcontrolapp

data class CommandRequest(
    val device_id: String,
    val command_type: String,
    val payload: String = ""
)