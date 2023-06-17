package org.konkuk.placelist.weather

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.konkuk.placelist.R
import org.konkuk.placelist.main.MainActivity
import org.konkuk.placelist.weather.models.airquality.MeasuredValue
import org.konkuk.placelist.weather.models.weatherforecast.WeatherForecast
import org.konkuk.placelist.weather.`object`.GpsConverter
import java.text.SimpleDateFormat
import java.util.*

class WeatherAlarmReceiver : BroadcastReceiver() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var cancellationTokenSource: CancellationTokenSource? = null
    private val scope = MainScope()

    var measuredValue: MeasuredValue? = null
    private var weatherForecasts: List<WeatherForecast?>? = null
    lateinit var stationName: String

    lateinit var cal: Calendar
    lateinit var baseDate: String
    lateinit var baseTime: String

    private var hour: String? = null
    private var minute: String? = null


    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback
    var startupdate = false
    var pFlag = false

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.getIntExtra("code", 0) == 1000) {
            Log.d("weatherAlarmReceiver", "alarm Ring")
            hour = intent.getStringExtra("hour")
            minute = intent.getStringExtra("minute")
            getLocation(context)
        }

        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            setAlarm(context)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(context: Context) {
        cancellationTokenSource = CancellationTokenSource()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationProviderClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource!!.token
        )
            .addOnSuccessListener { location ->
                if (location == null) {
                    initLocation(context)
                }
                else {
                    fetchWeatherData(context, location.longitude, location.latitude)
                }
            }
    }

    private fun fetchWeatherData(context: Context, latitude:Double, longitude: Double) {
        scope.launch {
            val monitoringStation =
                Repository.getNearbyMonitoringStation(latitude, longitude)
            measuredValue =
                Repository.getLatestAirQualityData(monitoringStation!!.stationName!!)
            stationName = monitoringStation.stationName.toString()

            Log.d("weather", measuredValue?.o3Grade.toString())
            Log.d("weather", measuredValue?.pm10Grade.toString())
            Log.d("weather", measuredValue?.pm25Grade.toString())

            setBaseTime()

            val curPoint = GpsConverter.dfsXyConv(latitude, longitude)
            weatherForecasts = Repository.getWeatherForecasts(
                baseDate,
                baseTime,
                "JSON",
                curPoint.x,
                curPoint.y
            )
            withContext(Dispatchers.Main) {
                makeNotification(context)
                setNextAlarm(context)
                stopLocationUpdate()
            }
        }
    }
    private fun initLocation(context: Context) {
        locationRequest = LocationRequest.create().apply {
            interval = 1000 * 60 * 60
            fastestInterval = 1000 * 5
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        //val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(location: LocationResult) {
                if(location.locations.size == 0) {
                    Log.d("weatherAlarmReceiver", "위치 요청 실패")
                    return
                }
                Log.d("locationRequest", location.locations[location.locations.size - 1].latitude.toString() + " " + location.locations[location.locations.size - 1].longitude.toString())
                fetchWeatherData(context, location.locations[location.locations.size - 1].latitude, location.locations[location.locations.size - 1].longitude)
            }
        }
        startLocationUpdate(context)
    }

    private fun startLocationUpdate(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        startupdate = true
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
        Log.d("locationUpdate", "startLocationUpdates()")

    }
    private fun stopLocationUpdate() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        startupdate=false
    }

    private fun setNextAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = Intent(context, WeatherAlarmReceiver::class.java).let {
            it.putExtra("code", 1000)
            it.putExtra("hour", hour)
            it.putExtra("minute", minute)
            PendingIntent.getBroadcast(
                context,
                1000,
                it,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour!!.toInt())
            set(Calendar.MINUTE, minute!!.toInt())
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        //Toast.makeText(context, "set Alarm on $hour : $minute", Toast.LENGTH_SHORT).show()
    }

    private fun makeNotification(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "날씨 알림"
            val channelName = "PlaceList"
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
                    .apply {
                        enableLights(true)
                        lightColor = Color.BLUE
                        lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                    }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                1000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setContentTitle("날씨 알림")
                .setContentText("$stationName 예보 확인하기")
                .setSmallIcon(R.drawable.img_p)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(getWeatherForecastMsg())
                )
            val notification = notificationBuilder.build()
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.apply {
                createNotificationChannel(notificationChannel)
                notify(0, notification)
            }
        }
    }

    private fun getWeatherForecastMsg(): CharSequence? {
        var msg = ""
        weatherForecasts?.forEach {
            //PTY: 강수정보
            if (it?.category == "PTY") {
                when (it?.fcstValue) {
                    "1" -> {
                        msg += getPtyMsg(it?.fcstDate, it?.fcstTime, "비")
                        pFlag = true
                        return@forEach
                    }
                    "2" -> {
                        msg += getPtyMsg(it?.fcstDate, it?.fcstTime, "비/눈")
                        pFlag = true
                        return@forEach
                    }
                    "3" -> {
                        msg += getPtyMsg(it?.fcstDate, it?.fcstTime, "눈")
                        pFlag = true
                        return@forEach
                    }
                    "4" -> {
                        msg += getPtyMsg(it?.fcstDate, it?.fcstTime, "소나기")
                        pFlag = true
                        return@forEach
                    }
                }
            }
        }
        if(!pFlag) msg += "24시간 이내 강수 예보 없음\n"
        msg += "오존 등급: ${measuredValue?.o3Grade.toString()}\n" +
                "미세먼지 등급: ${measuredValue?.pm10Grade.toString()}\n" +
                "초미세먼지 등급: ${measuredValue?.pm10Grade.toString()}\n"
        return msg
    }

    private fun getPtyMsg(fcstDate: String?, fcstTime: String?, pty: String): String {
        val today = if (fcstDate == getDate()) {
            "오늘"
        } else {
            "내일"
        }
        return "$today ${fcstTime}시에 $pty 예보가 있어요."
    }

    private fun getDate(): String {
        val date = Date(System.currentTimeMillis())
        val dataFormat = SimpleDateFormat("yyyyMMdd")
        return dataFormat.format(date)
    }

    private fun setBaseTime() {
        cal = Calendar.getInstance()

        when (SimpleDateFormat("HH", Locale.getDefault()).format(cal.time)) {
            "00", "01" -> {
                cal.add(Calendar.DATE, -1).toString()
                baseTime = "2300"
            }
            "02", "03", "04" -> {
                cal = Calendar.getInstance()
                baseTime = "0200"
            }
            "05", "06", "07" -> {
                cal = Calendar.getInstance()
                baseTime = "0500"
            }
            "08", "09", "10" -> {
                cal = Calendar.getInstance()
                baseTime = "0800"
            }
            "11", "12", "13" -> {
                cal = Calendar.getInstance()
                baseTime = "1100"
            }
            "14", "15", "16" -> {
                cal = Calendar.getInstance()
                baseTime = "1400"
            }
            "17", "18", "19" -> {
                cal = Calendar.getInstance()
                baseTime = "1700"
            }
            "20", "21", "22" -> {
                cal = Calendar.getInstance()
                baseTime = "2000"
            }
            "23" -> {
                cal = Calendar.getInstance()
                baseTime = "2300"
            }
        }
        baseDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
    }

    //부팅시에 알람 설정
    private fun setAlarm(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val alarmOnOff = prefs.getBoolean("weatherAlarm", true)

        val hour = prefs.getString("hour", "6")
        val minute = prefs.getString("minute", "0")

        val pendingIntent = Intent(context, WeatherAlarmReceiver::class.java).let {
            it.putExtra("code", 1000)
            it.putExtra("hour", hour)
            it.putExtra("minute", minute)
            PendingIntent.getBroadcast(
                context,
                1000,
                it,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (alarmOnOff) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour!!.toInt())
                set(Calendar.MINUTE, minute!!.toInt())
            }
            //이미 지난 시간 설정한 경우 다음날 같은 시간으로 설정
            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1)
            }
            //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
            )
            //Toast.makeText(context, "set Alarm on $hour : $minute", Toast.LENGTH_SHORT).show()
        } else {
            alarmManager.cancel(pendingIntent)
            //Toast.makeText(context, "set Alarm off", Toast.LENGTH_SHORT).show()
        }
    }
}