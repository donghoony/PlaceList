package org.konkuk.placelist

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class MyViewModel : ViewModel() {
    val location = MutableLiveData<LatLng>()
    var detectRange = MutableLiveData<Float>(100f)
    fun setLiveData(location: LatLng){
         this.location.postValue(location)
    }
    fun setRange(range: Float){
        this.detectRange.postValue(range)
        Log.i("Range", "Range changed to $range")
    }

}