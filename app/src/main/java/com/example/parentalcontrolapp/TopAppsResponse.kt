package com.example.parentalcontrolapp

import com.google.gson.annotations.SerializedName

data class TopAppsResponse(

    @SerializedName("top_apps")
    val topApps: List<TopApp>

)

data class TopApp(

    val package_name: String,
    val duration: Int

)