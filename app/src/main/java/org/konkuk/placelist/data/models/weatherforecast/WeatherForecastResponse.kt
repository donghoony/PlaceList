package org.konkuk.placelist.data.models.weatherforecast


import com.google.gson.annotations.SerializedName

data class WeatherForecastResponse(
    @SerializedName("response")
    val response: Response?
)