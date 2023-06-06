package org.konkuk.placelist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class MyViewModel : ViewModel() {
    val location = MutableLiveData<LatLng>()
    var detectRange: Float = 100.0f
    fun setLiveData(location: LatLng){
         this.location.postValue(location)
    }

}