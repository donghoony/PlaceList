package org.konkuk.placelist.data

import org.konkuk.placelist.data.models.airquality.MeasuredValue
import org.konkuk.placelist.data.models.monitoringstation.MonitoringStation
import org.konkuk.placelist.data.models.weatherforecast.WeatherForecast
import org.konkuk.placelist.data.services.AirKoreaApiService
import org.konkuk.placelist.data.services.KakaoLocalApiService
import org.konkuk.placelist.data.services.KmaApiService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.konkuk.placelist.BuildConfig
import org.konkuk.placelist.data.url.Url
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object Repository {
    var gson = GsonBuilder().setLenient().create()

    suspend fun getNearbyMonitoringStation(latitude: Double, longitude: Double): MonitoringStation? {
        val tmCoordinates = kakaoLocalApiService.getTmCoordinates(longitude, latitude)
            .body()
            ?.documents
            ?.firstOrNull()

        val tmX = tmCoordinates?.x
        val tmY = tmCoordinates?.y

        return airKoreaApiService.getNearbyMonitoringStation(tmX!!, tmY!!)
            .body()
            ?.response
            ?.body
            ?.monitoringStations
            ?.minByOrNull {
                it?.tm ?: Double.MAX_VALUE
            }
    }

    suspend fun getWeatherForecasts(base_date: String, base_time: String, dataType:  String,  x: Int, y: Int): List<WeatherForecast?>? =
        kmaApiService.getWeatherForecasts(base_date, base_time,dataType, x, y)
            .body()
            ?.response
            ?.body
            ?.weatherForecasts
            ?.weatherForecast


    suspend fun getLatestAirQualityData(stationName: String): MeasuredValue?=
        airKoreaApiService.getRealtimeAirQualities(stationName)
            .body()
            ?.response
            ?.body
            ?.measuredValues
            ?.firstOrNull()

    private val kakaoLocalApiService: KakaoLocalApiService by lazy{
        Retrofit.Builder()
            .baseUrl(Url.KAKAO_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(buildHttpClient())
            .build()
            .create()
    }

    private val airKoreaApiService: AirKoreaApiService by lazy{
        Retrofit.Builder()
            .baseUrl(Url.PUBLIC_DATA_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(buildHttpClient())
            .build()
            .create()
    }

    private val kmaApiService: KmaApiService by lazy{
        Retrofit.Builder()
            .baseUrl(Url.PUBLIC_DATA_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(buildHttpClient())
            .build()
            .create()
    }

    private fun buildHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if(BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            ).build()
}