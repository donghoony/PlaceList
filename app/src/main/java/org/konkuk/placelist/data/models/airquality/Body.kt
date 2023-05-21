package org.konkuk.placelist.data.models.airquality

import com.google.gson.annotations.SerializedName

data class Body(
    @SerializedName("items")
    val measuredValues: List<MeasuredValue?>?
)