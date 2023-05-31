package org.konkuk.placelist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.konkuk.placelist.domain.enums.Coordinate

class MyViewModel:ViewModel() {
    val location= MutableLiveData<Coordinate>()
    fun setLiveData(loca: Coordinate){
         location.postValue(loca)
    }
}