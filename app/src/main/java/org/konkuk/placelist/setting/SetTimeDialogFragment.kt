package org.konkuk.placelist.setting

import android.content.SharedPreferences
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import org.konkuk.placelist.databinding.FragmentSetTimeBinding

class SetTimeDialogFragment : DialogFragment() {
    lateinit var binding: FragmentSetTimeBinding
    lateinit var act: SettingsActivity
    lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetTimeBinding.inflate(inflater, container, false)
        dialog?.window?.setGravity(Gravity.CENTER_HORIZONTAL)
        with(dialog?.window?.attributes) {
            this?.width = ViewGroup.LayoutParams.MATCH_PARENT
            this?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            this?.verticalMargin = 0f
            this?.horizontalMargin = 0f
        }
        initTime()
        initButtons()
        return binding.root
    }

    private fun initTime() {
        act = context as SettingsActivity
        prefs = PreferenceManager.getDefaultSharedPreferences(act)

        val hour = prefs?.getString("hour", "6").toString()
        val minute = prefs?.getString("minute", "0").toString()

        val isSystem24Hour = DateFormat.is24HourFormat(context)

        binding.apply {
            timePicker.hour = hour.toInt()
            timePicker.minute = minute.toInt()
            timePicker.setIs24HourView(isSystem24Hour)
        }
    }

    private fun initButtons() {
        binding.apply {
            closeBtn.setOnClickListener {
                dismiss()
            }
            submitBtn.setOnClickListener {
                prefs!!.edit()
                    .putString("hour", "${timePicker.hour}")
                    .putString("minute", "${timePicker.minute}")
                    .apply()
                dismiss()
            }
        }
    }
}