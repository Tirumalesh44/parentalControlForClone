package com.example.parentalcontrolapp

data class SetLimitRequest(
    val device_id: String,
    val limit: Int
)