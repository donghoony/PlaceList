package org.konkuk.placelist

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import org.konkuk.placelist.databinding.FragmentAddPlaceBinding

class AddPlaceDialogFragment : DialogFragment() {
    lateinit var binding: FragmentAddPlaceBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
}