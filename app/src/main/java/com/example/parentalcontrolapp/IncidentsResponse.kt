package com.example.parentalcontrolapp

import com.google.gson.annotations.SerializedName

data class IncidentsResponse(

    @SerializedName("incident_count")
    val incidentCount: Int,

    val incidents: List<IncidentItem>
)

data class IncidentItem(

    val started_at: String,
    val ended_at: String?,
    val peak_risk: Double,
    val status: String,

    @SerializedName("duration_seconds")
    val durationSeconds: Int
)