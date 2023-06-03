package org.konkuk.placelist.dao

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun fromLocation(location: LatLng): String{
        return Gson().toJson(location)
    }

    @TypeConverter
    fun toLocation(s: String): LatLng {
        return Gson().fromJson(s, LatLng::class.java)
    }
}