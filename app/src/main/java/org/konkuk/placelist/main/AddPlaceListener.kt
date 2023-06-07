package org.konkuk.placelist.main

import com.google.android.gms.maps.model.LatLng

interface AddPlaceListener {
    fun addPlace(id: Int, name: String, coordinate: LatLng, radius: Float)

}
