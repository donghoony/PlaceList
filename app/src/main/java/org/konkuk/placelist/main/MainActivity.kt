package org.konkuk.placelist.main

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.konkuk.placelist.MyGeofence
import org.konkuk.placelist.PlacesListDatabase
import org.konkuk.placelist.databinding.ActivityMainBinding
import org.konkuk.placelist.domain.Place
import org.konkuk.placelist.place.PlacesActivity
import org.konkuk.placelist.setting.SettingsActivity
import org.konkuk.placelist.weather.WeatherAlarmReceiver
import java.util.*
import java.util.Calendar
import kotlin.math.abs

class MainActivity : AppCompatActivity(), AddPlaceListener {
    lateinit var binding: ActivityMainBinding
    lateinit var placeAdapter: PlaceAdapter
    lateinit var myGeofence: MyGeofence
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initButton()
        initPlaceView()
        getPermissions()
        initSettings()
        initGeofence()
        setWeatherAlarm()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // Force Light Mode
    }
    //geofence 객체 생성, database 삭제 추가 변경시 이 객체에서 ChangeData() 함수 호출해주면 됨
    private fun initGeofence(){
        myGeofence= MyGeofence.makeInstance(this)
    }
    override fun onStart() {
        super.onStart()
        if(this::placeAdapter.isInitialized) placeAdapter.refresh()
    }


    private fun initSettings() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        if (prefs.getString("hour", "") == "" ||
            prefs.getString("minute", "") == ""
        ) {
            prefs.edit()
                .putString("hour", "6")
                .putString("minute", "0")
                .apply()
        }
    }

    private fun setWeatherAlarm() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val alarmOnOff = prefs.getBoolean("weatherAlarm", true)

        val hour = prefs.getString("hour", "6")
        val minute = prefs.getString("minute", "0")

        val pendingIntent = Intent(this, WeatherAlarmReceiver::class.java).let {
            it.putExtra("code", 1000)
            it.putExtra("hour", hour)
            it.putExtra("minute",minute)
            PendingIntent.getBroadcast(this, 1000, it, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if(!alarmManager.canScheduleExactAlarms()) {
            Intent().also {
                it.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                this.startActivity(intent)
            }
        }

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
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
            )
        } else {
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun initPlaceView() {
        val db = PlacesListDatabase.getDatabase(this)
        binding.placelist.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        CoroutineScope(Dispatchers.IO).launch {
            val items = db.placesDao().getAll() as ArrayList<Place>
            placeAdapter = PlaceAdapter(db, items)
            placeAdapter.itemClickListener = object : PlaceAdapter.OnItemClickListener {
                override fun onItemClick(data: Place, pos: Int) {
                    val intent = Intent(this@MainActivity, PlacesActivity::class.java)
                    intent.putExtra("place", data)
                    startActivity(intent)
                }
            }
            withContext(Dispatchers.Main){
                binding.placelist.adapter = placeAdapter
            }
        }
        val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {return true}
            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val p = Paint()
                    var icon: Bitmap
                    if (dX < 0) {
                        icon = BitmapFactory.decodeResource(resources, org.konkuk.placelist.R.drawable.btn_trash_1)
                        val h = abs((itemView.top - itemView.bottom) * 2 / 3)
                        val w = h*2/3
                        icon = Bitmap.createScaledBitmap(icon, w, h, false)
                        p.color = Color.parseColor("#FF5959")
                        c.drawRoundRect(itemView.right.toFloat()-20 + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat()-10, 10f, 10f, p)
                        c.drawBitmap(icon, itemView.right.toFloat() - w - 20, itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - h  + 10) / 2, p)
                    }
                    val alpha = MaterialColors.ALPHA_FULL - abs(dX) / viewHolder.itemView.width.toFloat()
                    viewHolder.itemView.alpha = alpha
                    viewHolder.itemView.translationX = dX
                }
                else super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("장소 삭제")
                    .setMessage("장소 내 할 일이 모두 삭제됩니다.\n정말 삭제하시겠어요?")
                    .setPositiveButton("삭제"){_, _ ->
                        CoroutineScope(Dispatchers.IO).launch{
                            db.placesDao().delete(placeAdapter.items[viewHolder.adapterPosition])
                            myGeofence.removeGeofence(placeAdapter.items[viewHolder.adapterPosition].id)
                            withContext(Dispatchers.Main) {
                                placeAdapter.removeItem(viewHolder.adapterPosition)
                            }
                        }
                    }
                    .setNegativeButton("취소"){d, _ ->
                        placeAdapter.refresh()
                        d.dismiss()
                    }
                    .create()
                    .show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.placelist)
    }


    private fun getPermissions() {
        lateinit var dialog: AlertDialog
        lateinit var backgroundDialog: AlertDialog

        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }

        fun checkPermission(permission: String) =
            ActivityCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED

        fun openPermissionSettings() = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.fromParts("package", packageName, null)
        }.run(::startActivity)

        val multipleLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { map ->
            if (map.all { permission -> permission.value }) {
                if (!checkPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                    backgroundDialog.show()
            }
        }

        // 추후에 Dialog 커스텀 필요할 수 있음 -> Fragment로 변경 등
        dialog = AlertDialog.Builder(this)
            .setTitle("권한 요청")
            .setMessage("어플리케이션을 사용하기 위해서는 권한이 필요합니다.\n권한 요청을 승인해 주세요.")
            .setPositiveButton("확인") { _, _ ->
                multipleLauncher.launch(permissions)
            }
            .setNegativeButton("종료") { _, _ ->
                finish()
            }
            .create()

        backgroundDialog = AlertDialog.Builder(this)
            .setTitle("백그라운드 권한 요청")
            .setMessage("권한 - 위치 탭에서 '항상 허용'에 체크해주세요")
            .setPositiveButton("확인") { _, _ ->
                openPermissionSettings()
            }
            .setNegativeButton("종료") { _, _ ->
                finish()
            }
            .create()

        if (!permissions.all { permission -> checkPermission(permission) }) dialog.show()
        else if (!checkPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)) backgroundDialog.show()
    }

    private fun initButton() {
        binding.btnPlus.setOnClickListener {
            AddPlaceDialogFragment().show(
                supportFragmentManager, "AddPlace"
            )
        }
        binding.setting.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun addPlace(id: Long, name: String, latitude: String, longitude: String, radius: Float) {
        placeAdapter.addPlace(id, name, latitude, longitude, radius, myGeofence)
    }
}