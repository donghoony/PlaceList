package org.konkuk.placelist.weather.services

import org.konkuk.placelist.BuildConfig.PUBLIC_DATA_SERVICE_KEY
import org.konkuk.placelist.weather.models.weatherforecast.WeatherForecastResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface KmaApiService {

    @GET("1360000/VilageFcstInfoService_2.0/getVilageFcst" + "?serviceKey=${PUBLIC_DATA_SERVICE_KEY}" + "&returnType=json" + "&numOfRows=290")
    suspend fun getWeatherForecasts(
        @Query("base_date") base_date: String,
        @Query("base_time") base_time: String,
        @Query("dataType") dataType: String,
        @Query("nx") x: Int,
        @Query("ny") y: Int
    ): Response<WeatherForecastResponse>
}