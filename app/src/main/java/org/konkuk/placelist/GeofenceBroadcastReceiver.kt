package org.konkuk.placelist


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.konkuk.placelist.domain.Place
import org.konkuk.placelist.domain.enums.PlaceSituation
import org.konkuk.placelist.main.MainActivity
import java.util.*
import kotlin.collections.ArrayList

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private val CHANNEL_ID = "my_channel_id"
    private var notificationIdCounter = 0

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("지오펜스 리시버", "지오펜스 리시버")
        val geofencingEvent = GeofencingEvent.fromIntent(intent!!)

        if (geofencingEvent!!.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition    // 발생 이벤트 타입

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
        ) {
            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            val transitionMsg = when (geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> "Enter"
                Geofence.GEOFENCE_TRANSITION_EXIT -> "Exit"
                else -> "DWELL"
            }
            if (triggeringGeofences != null) {
                triggeringGeofences.forEach {
                    Log.d("it.requestId", it.requestId)
                    Log.d("transitionMsg", transitionMsg)

                    CoroutineScope(Dispatchers.IO).launch {

                        val db = PlacesListDatabase.getDatabase(context!!)
                        val placeitems = db.placesDao().getAll() as ArrayList<Place>
                        var p: Place? = null
                        for (now in placeitems) {
                            if (now.id.toString() == it.requestId.toString()) {
                                p = now
                            }
                        }
                        val calendar = Calendar.getInstance()
                        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

                        //Todo정보 items로 가져오기 이 정보 가공해서 showNotification으로 알림 발송
                        val items = db.TodoDao().findTodoByPlaceId(p!!.id.toString().toInt())
//                        if (items.size != 0) {
                        var Msg: String = ""
                        for (i in items) {
                            if (!i.isCompleted && ((i.repeatDays and (1 shl (dayOfWeek - 1)) != 0)|| i.repeatDays == 0)) {
                                if (i.situation == PlaceSituation.BOTH) {
                                    Msg += i.name + " "
                                } else if (i.situation == PlaceSituation.ENTER && geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                                    Msg += i.name + " "
                                } else if (i.situation == PlaceSituation.ESCAPE && geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                                    Msg += i.name + " "
                                }
                            }

                        }
                        createNotificationChannel(context!!)
                        showNotification(context!!, p.name + " " + transitionMsg, Msg)
                        if (!p!!.isEnter && geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                createNotificationChannel(context!!)
                            }
                            //보내는 메세지 it.requestid== 장소이름 , transitionMsg enter or exit msg는 items가공해서 보낼 메세지 작성
                            showNotification(context!!, p.name + " " + transitionMsg, Msg)
                            p.isEnter = true
                            db.placesDao().update(p)
                        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                createNotificationChannel(context!!)
                            }
                            //보내는 메세지 it.requestid== 장소이름 , transitionMsg enter or exit msg는 items가공해서 보낼 메세지 작성
                            showNotification(context!!, p.name + " " + transitionMsg, Msg)
                            p.isEnter = false
                            db.placesDao().update(p)
                        }

                    }
                    Log.d("리시버 엔드", "리시버 엔드")

                }

            }

        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "My Channel"
            val channelDescription = "My Channel Description"
            val channelImportance = NotificationManager.IMPORTANCE_DEFAULT

            val notificationChannel =
                NotificationChannel(CHANNEL_ID, channelName, channelImportance)
            notificationChannel!!.description = channelDescription

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel!!)
        }
    }

    private fun showNotification(context: Context, title: String, msg: String) {
        val notificationId = notificationIdCounter++

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.img_p)
            .setContentTitle(title)
            .setContentText(msg)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(notificationId, builder.build())
    }
}