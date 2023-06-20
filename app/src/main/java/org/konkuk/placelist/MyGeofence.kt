package org.konkuk.placelist

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

class MyGeofence(private val context: Context) :Serializable {
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

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

    fun addGeofence(reqId: Long, geo: LatLng, radius: Float){
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val request = GeofencingRequest.Builder().addGeofence(buildGeofence(reqId.toString(), geo, radius)).build()
        geofencingClient.addGeofences(request, geofencePendingIntent)
            .run {
                addOnSuccessListener {
                    Log.d("geofence add success", "success")
                }
                addOnFailureListener {
                    Log.d("geofence add fail", "fail")
                }
            }
    }

    private fun buildGeofence(
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

    fun removeGeofence(id: Long) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        geofencingClient.removeGeofences(listOf(id.toString())).run{
            addOnSuccessListener {
                Log.d("geofence remove success", "success")
            }
            addOnFailureListener {
                Log.d("geofence remove fail", "fail")
            }
        }
    }
}