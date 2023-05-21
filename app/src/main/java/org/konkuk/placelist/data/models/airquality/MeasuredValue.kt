package org.konkuk.placelist.data.models.airquality

import com.google.gson.annotations.SerializedName

data class MeasuredValue(
    @SerializedName("coFlag") // 일산화탄소 플래그
    val coFlag: Any?,
    @SerializedName("coGrade") // 일산화탄소 지수
    val coGrade: String?,
    @SerializedName("coValue") // 일산화탄소 농도
    val coValue: String?,
    @SerializedName("dataTime") // 측정일
    val dataTime: String?,
    @SerializedName("khaiGrade") // 통합 대기 환경 지수
    val khaiGrade: String?,
    @SerializedName("khaiValue") // 통합 대기 환경 수치
    val khaiValue: String?,
    @SerializedName("mangName") // 측정망 정보
    val mangName: String?,
    @SerializedName("no2Flag") // 이산화질소 플래그
    val no2Flag: Any?,
    @SerializedName("no2Grade") // 이산화질소 지수
    val no2Grade: String?,
    @SerializedName("no2Value") // 이산화질소 농도
    val no2Value: String?,
    @SerializedName("o3Flag") // 오존 플래그
    val o3Flag: Any?,
    @SerializedName("o3Grade") // 오존 지수
    val o3Grade: String?,
    @SerializedName("o3Value") // 오존 농도
    val o3Value: String?,
    @SerializedName("pm10Flag") // 미세먼지 플래그
    val pm10Flag: Any?,
    @SerializedName("pm10Grade") // 미세먼지  24시간 등급
    val pm10Grade: String?,
    @SerializedName("pm10Grade1h") // 미세먼지 1시간 등급
    val pm10Grade1h: String?,
    @SerializedName("pm10Value") // 미세먼지 농도
    val pm10Value: String?,
    @SerializedName("pm10Value24") // 미세먼지 24시간 예측 이동 농도
    val pm10Value24: String?,
    @SerializedName("pm25Flag") // 초미세먼지 플래그
    val pm25Flag: Any?,
    @SerializedName("pm25Grade") // 초미세먼지  24시간 등급
    val pm25Grade: String?,
    @SerializedName("pm25Grade1h") // 초미세먼지 1시간 등급
    val pm25Grade1h: String?,
    @SerializedName("pm25Value") // 초미세먼지 농도
    val pm25Value: String?,
    @SerializedName("pm25Value24") // 초미세먼지 24시간 예측 이동 농도
    val pm25Value24: String?,
    @SerializedName("so2Flag") // 이황산가스 플래그
    val so2Flag: Any?,
    @SerializedName("so2Grade") // 이황산가스 지수
    val so2Grade: String?,
    @SerializedName("so2Value") // 이황산가수 농도
    val so2Value: String?
)