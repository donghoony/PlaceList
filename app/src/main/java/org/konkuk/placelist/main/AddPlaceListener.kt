package org.konkuk.placelist.main

interface AddPlaceListener {
    fun addPlace(id: Long, name: String, latitude: String, longitude: String, radius: Float)

}
