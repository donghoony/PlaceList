package org.konkuk.placelist.data.models.weatherforecast


import com.google.gson.annotations.SerializedName

data class WeatherForecasts(
    @SerializedName("item")
    val weatherForecast: List<WeatherForecast?>?
)