package com.example.parentalcontrolapp

import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("/register-parent")
    fun registerParent(
        @Query("device_id") deviceId: String,
        @Query("fcm_token") fcmToken: String
    ): Call<Map<String, String>>

    @POST("/commands/send")
    fun sendCommand(
        @Body request: CommandRequest
    ): Call<Map<String, String>>

    @GET("/usage/{device_id}")
    fun getUsage(
        @Path("device_id") deviceId: String
    ): Call<UsageResponse>

    @GET("/top-apps/{device_id}")
    fun getTopApps(
        @Path("device_id") deviceId: String
    ): Call<TopAppsResponse>

    @GET("/incidents/{device_id}")
    fun getIncidents(
        @Path("device_id") deviceId: String
    ): Call<IncidentsResponse>

    @POST("/set-limit")
    fun setLimit(
        @Body request: SetLimitRequest
    ): Call<Map<String, String>>

    // -------------------------
    // BLOCK APP
    // -------------------------

    @POST("/block-app")
    fun blockApp(
        @Body data: Map<String, String>
    ): Call<Map<String, String>>

    // -------------------------
    // UNBLOCK APP
    // -------------------------

    @POST("/unblock-app")
    fun unblockApp(
        @Body data: Map<String, String>
    ): Call<Map<String, String>>

    // -------------------------
    // GET BLOCKED APPS
    // -------------------------

    @GET("/blocked-apps/{device_id}")
    fun getBlockedApps(
        @Path("device_id") deviceId: String
    ): Call<Map<String, Any>>

    // -------------------------
    // GET INSTALLED APPS
    // -------------------------

    @GET("/apps/{device_id}")
    fun getInstalledApps(
        @Path("device_id") deviceId: String
    ): Call<Map<String, Any>>

    @GET("usage-summary/{deviceId}")
    fun getUsageSummary(
        @Path("deviceId") deviceId:String
    ): Call<Map<String,Any>>
}