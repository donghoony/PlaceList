package org.konkuk.placelist.weather.models.airquality

import androidx.annotation.ColorRes
import com.google.gson.annotations.SerializedName

enum class Grade(val label: String) {

    @SerializedName("1")
    GOOD("좋음"),
    @SerializedName("2")
    NORMAL("보통"),
    @SerializedName("3")
    BAD("나쁨"),
    @SerializedName("4")
    AWFUL("매우 나쁨"),

    UNKNOWN("미측정");

    override fun toString(): String {
        return "$label"
    }
}