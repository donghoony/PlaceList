package org.konkuk.placelist

import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import org.konkuk.placelist.databinding.FragmentAddPlaceBinding
import org.konkuk.placelist.domain.enums.Coordinate
import java.util.*
import android.os.Bundle as Bundle1

class AddPlaceDialogFragment : DialogFragment() {
    lateinit var binding: FragmentAddPlaceBinding
    //val geocoder= Geocoder(requireActivity())
    //val myviewModel:MyViewModel by viewModels()
    val model:MyViewModel by activityViewModels()
    var text=""
    override fun onCreate(savedInstanceState: Bundle1?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle1?
    ): View {
        binding = FragmentAddPlaceBinding.inflate(inflater, container, false)
        dialog?.window?.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM)
        with (dialog?.window?.attributes){
            this?.width = ViewGroup.LayoutParams.MATCH_PARENT
            this?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            this?.verticalMargin = 0f
            this?.horizontalMargin = 0f
        }
        initButtons()
        initgeocoder()
        return binding.root
    }

    private fun initButtons() {

        with(binding){
            this.closeBtn.setOnClickListener {
                dismiss()
            }
            this.submitBtn.setOnClickListener {
                // TODO: Add Place in adapter
            }
        }


    }
    private fun initgeocoder(){
        val geocoder= Geocoder(requireActivity(), Locale.KOREA)
        model.location.observe(viewLifecycleOwner, Observer {
           /* binding.location.setText((geocoder.getFromLocation(it.latitude,it.longitude,1){

            }))*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(it.latitude,it.longitude,1){
                    it->
                    binding.location.setText(it[0].getAddressLine(0))
                }
            }
        })
        binding.location.setOnKeyListener { v, keyCode, event ->
            if(event.action==KeyEvent.ACTION_DOWN&&keyCode==KEYCODE_ENTER){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocationName(binding.location.text.toString(),1
                    ) { addresses ->
                        val cool: Coordinate =
                            Coordinate(addresses[0].longitude, addresses[0].latitude)
                        model.setLiveData(cool)

                    }
                }


            }
            true
        }
    }

}