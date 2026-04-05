package com.example.parentalcontrolapp

import com.google.gson.annotations.SerializedName

data class UsageResponse(

    @SerializedName("total_screen_time")
    val totalScreenTime: Int,

    val apps: List<AppUsageItem>
)

data class AppUsageItem(

    @SerializedName("package")
    val packageName: String,

    val duration: Int
)