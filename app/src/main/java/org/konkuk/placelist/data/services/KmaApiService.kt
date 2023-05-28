package org.konkuk.placelist.data.services

import org.konkuk.placelist.BuildConfig.PUBLIC_DATA_SERVICE_KEY
import org.konkuk.placelist.data.models.weatherforecast.WeatherForecastResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface KmaApiService {

    @GET("1360000/VilageFcstInfoService_2.0/getVilageFcst" + "?serviceKey=${PUBLIC_DATA_SERVICE_KEY}" + "&returnType=json" + "&numOfRows=200")
    suspend fun getWeatherForecasts(
        @Query("base_date") base_date: String,
        @Query("base_time") base_time: String,
        @Query("dataType") dataType: String,
        @Query("nx") x: Int,
        @Query("ny") y: Int
    ): Response<WeatherForecastResponse>
}