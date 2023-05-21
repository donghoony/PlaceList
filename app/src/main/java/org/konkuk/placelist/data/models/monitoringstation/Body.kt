package org.konkuk.placelist.data.models.monitoringstation

import com.google.gson.annotations.SerializedName

data class Body(
    @SerializedName("items")
    val monitoringStations: List<MonitoringStation?>?
)