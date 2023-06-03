package org.konkuk.placelist.main

import com.google.android.gms.maps.model.LatLng

interface AddPlaceListener {
    fun addPlace(name: String, coordinate: LatLng)
}
