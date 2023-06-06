package org.konkuk.placelist.main

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.maps.model.LatLng
import org.konkuk.placelist.MyViewModel
import org.konkuk.placelist.databinding.FragmentAddPlaceBinding
import java.util.Locale
import android.os.Bundle as Bundle1


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class AddPlaceDialogFragment : DialogFragment() {
    lateinit var binding: FragmentAddPlaceBinding
    lateinit var addPlaceListener: AddPlaceListener
    val model : MyViewModel by activityViewModels()
    var text = ""
    var selectedLocation = LatLng(0.0, 0.0)

    override fun onCreate(savedInstanceState: Bundle1?) {
        super.onCreate(savedInstanceState)
        try{
            addPlaceListener = context as AddPlaceListener
        } catch (e: ClassCastException) { Log.e("E", "Cast Failed")}
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
        initGeocoder()
        return binding.root
    }

    private fun initButtons() {
        with(binding){
            this.radiusSeekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val range = when(progress){
                        0 -> 100.0f
                        1 -> 200.0f
                        2 -> 500.0f
                        else -> 100.0f
                    }
                    model.setRange(range)
                    Log.i("Radius", "Radius changed into $range")
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            this.closeBtn.setOnClickListener {
                dismiss()
            }
            this.submitBtn.setOnClickListener {
                addPlaceListener.addPlace(binding.placename.text.toString(), selectedLocation, model.detectRange.value!!)
                dismiss()
            }
        }

    }
    private fun initGeocoder() {
        val geocoder = Geocoder(requireActivity(), Locale.KOREA)

        model.location.observe(viewLifecycleOwner) { it: LatLng ->
            geocoder.getFromLocation(it.latitude, it.longitude, 1) { location ->
                selectedLocation = LatLng(location[0].latitude, location[0].longitude)
            }
        }

        binding.location.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KEYCODE_ENTER) {
                geocoder.getFromLocationName(binding.location.text.toString(), 1){ addresses ->
                    Log.i("M", "${addresses[0].latitude}, ${addresses[0].longitude}")
                    model.setLiveData(LatLng(addresses[0].latitude, addresses[0].longitude))
                }
            }
            false
        }
    }

    override fun onResume() {
        super.onResume()
        context?.dialogFragmentResize(1f, 0.6f)
        val window = dialog!!.window
        window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
    }
    private fun Context.dialogFragmentResize(w: Float, h: Float) {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT < 30) {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val window = dialog?.window
            val x = (size.x * w).toInt()
            val y = (size.y * h).toInt()
            window?.setLayout(x, y)
        } else {
            val rect = windowManager.currentWindowMetrics.bounds
            val window = dialog?.window
            val x = (rect.width() * w).toInt()
            val y = (rect.height() * h).toInt()
            window?.setLayout(x, y)
        }
    }
}