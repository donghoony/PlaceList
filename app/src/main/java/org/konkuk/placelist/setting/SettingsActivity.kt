package org.konkuk.placelist.setting

import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.DialogPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import org.konkuk.placelist.R
import java.lang.reflect.Array.set
import java.time.LocalTime

class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)

    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val prefs = preferenceManager.sharedPreferences
            val preference = preferenceManager.findPreference<Preference>("weather_notification")
            preference?.setOnPreferenceClickListener {

                val hour = prefs?.getString("notification_hour", "6").toString()
                val minute = prefs?.getString("notification_minute", "0").toString()

                // Show the time picker
                val isSystem24Hour = DateFormat.is24HourFormat(context)
                val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
                val picker =
                    MaterialTimePicker.Builder()
                        .setTimeFormat(clockFormat)
                        .setPositiveButtonText("확인")
                        .setHour(hour.toInt())
                        .setMinute(minute.toInt())
                        .setTitleText("날씨 알림 시간 설정")
                        .build()

                picker.addOnPositiveButtonClickListener {
                    prefs!!.edit()
                        .putString("notification_hour", "${picker.hour}")
                        .putString("notification_minute", "${picker.minute}")
                        .apply()
                }
                val act = context as SettingsActivity
                picker.show(act.supportFragmentManager, "timepicker")
                true
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }
}