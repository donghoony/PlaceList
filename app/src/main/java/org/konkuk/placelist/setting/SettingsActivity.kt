package org.konkuk.placelist.setting

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import org.konkuk.placelist.R
import org.konkuk.placelist.weather.WeatherAlarmReceiver
import java.util.*

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

            val preference = preferenceManager.findPreference<Preference>("weatherTimer")
            preference?.setOnPreferenceClickListener {

                val act = context as SettingsActivity
                SetTimeDialogFragment().show(act.supportFragmentManager, "timePicker")

                true
            }
        }

    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        when(key) {
            "hour", "minute" -> {
                setWeatherAlarm(prefs)
            }
            "weatherAlarm" -> {
                setWeatherAlarm(prefs)
            }
        }
    }

    private fun setWeatherAlarm(prefs: SharedPreferences?) {
        val hour = prefs!!.getString("hour", "6")
        val minute = prefs!!.getString("minute", "0")
        val pendingIntent = Intent(this, WeatherAlarmReceiver::class.java).let {
            it.putExtra("code", 1000)
            it.putExtra("hour", hour)
            it.putExtra("minute",minute)
            PendingIntent.getBroadcast(this, 1000, it, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmOnOff = prefs!!.getBoolean("weatherAlarm", true)

        if(alarmOnOff) {
            val calendar = Calendar.getInstance().apply{
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour!!.toInt())
                set(Calendar.MINUTE, minute!!.toInt())
            }
            //이미 지난 시간 설정한 경우 다음날 같은 시간으로 설정
            if(calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1)
            }
            //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
            )
            //Toast.makeText(this, "set Alarm on $hour : $minute", Toast.LENGTH_SHORT).show()
        } else {
            alarmManager.cancel(pendingIntent)
            //Toast.makeText(this, "set Alarm off", Toast.LENGTH_SHORT).show()
        }
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