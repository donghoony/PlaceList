package org.konkuk.placelist


import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build.VERSION_CODES.S
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.Api.Client
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import org.konkuk.placelist.domain.Place
import java.io.Serializable


class MyGeofence(private val context: Context) :Serializable {
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    private val geofenceList: MutableList<Geofence> = mutableListOf()


    companion object {
        private var instance: MyGeofence? = null
        fun makeInstance(context: Context): MyGeofence {
            return instance ?: synchronized(this) {
                instance ?: MyGeofence(context).also { instance = it }
            }
        }
        fun getInstance(): MyGeofence {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        throw IllegalStateException("MyGeofence is not initialized.")
                    }
                }
            }
            return instance!!
        }

    }

//데이터베이스 데이터 추가 삭제나 변경 있을 시 Main 의 geofence 객체에서 이 함수 호출하면 됨
    fun ChangeData(items: ArrayList<Place>) {

        geofenceList.clear()
        for (now in items) {
            Log.d(now.name,now.latitude.toString())
            Log.d(now.longitude.toString(),now.detectRange.toString())

            geofenceList.add(
                getGeofence(
                    now.id.toString(),
                    LatLng(now.latitude.toDouble(), now.longitude.toDouble()),
                    now.detectRange
                )
            )
        }
        removegeo()
        addgeo()
    }

    private fun getGeofence(
        reqId: String,
        geo: LatLng,
        radius: Float
    ): Geofence {
        return Geofence.Builder()
            .setRequestId(reqId)    // 이벤트 발생시 BroadcastReceiver에서 구분할 id
            .setCircularRegion(geo.latitude, geo.longitude, radius)    // 위치 및 반경(m)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)        // Geofence 만료 시간
            .setLoiteringDelay(1000)                            // 머물기 체크 시간
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER                // 진입 감지시
                        or Geofence.GEOFENCE_TRANSITION_EXIT    //퇴출 감지시
                        or Geofence.GEOFENCE_TRANSITION_DWELL
            )
            .build()
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    private fun getGeofencingRequest(list: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            // Geofence 이벤트는 진입시 부터 처리할 때
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(list)    // Geofence 리스트 추가
        }.build()
    }


    private fun addgeo() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (geofenceList.size != 0) {
            geofencingClient.addGeofences(getGeofencingRequest(geofenceList), geofencePendingIntent)
                .run {
                    addOnSuccessListener {
                        Log.d("geofence add success", "success")
                    }
                    addOnFailureListener {
                        Log.d("geofence add fail", "fail")
                    }
                }
        }

    }

    private fun removegeo() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                Log.d("geofence remove success", "success")
            }
            addOnFailureListener {
                Log.d("geofence remove fail", "fail")
            }
        }
    }
}