package org.konkuk.placelist.main

interface AddPlaceListener {
    fun addPlace(id: Int, name: String, latitude: String, longitude: String, radius: Float)

}
