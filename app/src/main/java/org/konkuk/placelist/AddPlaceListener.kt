package org.konkuk.placelist

import com.google.android.gms.maps.model.LatLng

interface AddPlaceListener {
    fun addPlace(name: String, coordinate: LatLng)
}
