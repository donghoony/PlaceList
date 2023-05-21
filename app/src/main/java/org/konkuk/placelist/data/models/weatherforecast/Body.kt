package org.konkuk.placelist.data.models.weatherforecast


import com.google.gson.annotations.SerializedName

data class Body(
    @SerializedName("dataType")
    val dataType: String?,
    @SerializedName("items")
    val weatherForecasts: WeatherForecasts?
)