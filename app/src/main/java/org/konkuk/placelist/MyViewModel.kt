package org.konkuk.placelist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class MyViewModel:ViewModel() {
    val location = MutableLiveData<LatLng>()
    fun setLiveData(loca: LatLng){
         location.postValue(loca)
    }
}