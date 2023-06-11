package org.konkuk.placelist.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "places")
data class Place(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "place_id") val id: Int,
    var name: String,
    var latitude: String,
    var longitude: String,
    var detectRange: Float
    ) : Serializable
