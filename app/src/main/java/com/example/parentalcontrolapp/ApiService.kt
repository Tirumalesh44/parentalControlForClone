package com.example.parentalcontrolapp

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("/register-parent")
    fun registerParent(
        @Query("device_id") deviceId: String,
        @Query("fcm_token") fcmToken: String
    ): Call<Map<String, String>>
}