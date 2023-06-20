package org.konkuk.placelist


import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.konkuk.placelist.domain.Place
import org.konkuk.placelist.domain.enums.PlaceSituation
import org.konkuk.placelist.place.PlacesActivity
import java.util.Calendar

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private val CHANNEL_ID = "todo_channel"
    private var notificationIdCounter = 1 // Weather : 0

    private fun checkTrigger(desiredSituation: PlaceSituation, transition: Int) : Boolean{
        if (desiredSituation == PlaceSituation.BOTH &&
            (transition == GEOFENCE_TRANSITION_ENTER || transition == GEOFENCE_TRANSITION_EXIT)) return true
        if (desiredSituation == PlaceSituation.ENTER && transition == GEOFENCE_TRANSITION_ENTER) return true
        if (desiredSituation == PlaceSituation.ESCAPE && transition == GEOFENCE_TRANSITION_EXIT) return true
        return false
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("지오펜스 리시버", "지오펜스 리시버")
        val geofencingEvent = GeofencingEvent.fromIntent(intent!!)!!
        if (geofencingEvent.hasError()) return // Error message not used

        val geofenceTransition = geofencingEvent.geofenceTransition    // 발생 이벤트 타입
        if (geofenceTransition == GEOFENCE_TRANSITION_DWELL) return // 세 가지 경우밖에 없으므로 그 경우가 아니면 즉시 리턴

        val triggeringGeofences = geofencingEvent.triggeringGeofences!!
        val transitionMsg = when (geofenceTransition) {
            GEOFENCE_TRANSITION_ENTER -> "에 들어왔어요!"
            GEOFENCE_TRANSITION_EXIT -> "에서 나갔어요!"
            else -> "DWELL"
        }

        triggeringGeofences.forEach {
            Log.d("Geofence", "${it.requestId} Triggered $transitionMsg")

            val calendar = Calendar.getInstance()
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            CoroutineScope(Dispatchers.IO).launch {
                val db = PlacesListDatabase.getDatabase(context!!)
                val place = db.placesDao().findByPlaceId(it.requestId.toLong()) ?: return@launch
                val todoList = db.TodoDao().findByPlaceId(place.id)
                var notificationMessage = ""
                for (todo in todoList) {
                    if (todo.isCompleted) continue
                    if (!checkTrigger(todo.situation, geofenceTransition)) continue
                    if ((todo.repeatDays and (1 shl (dayOfWeek - 1)) != 0) || todo.repeatDays == 0) {
                        notificationMessage += todo.name + "\n"
                    }
                }
                createNotificationChannel(context)
                if (notificationMessage.isNotBlank())
                    showNotification(context, place, place.name + transitionMsg + " 잊으신 일은 없으신가요?", notificationMessage.trimEnd())
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        val channelName = "할 일 알림"
        val channelDescription = "장소를 들어오고 나갈 때 알림을 보냅니다."
        val channelImportance = NotificationManager.IMPORTANCE_HIGH

        val notificationChannel =
            NotificationChannel(CHANNEL_ID, channelName, channelImportance).apply {
                enableLights(true)
                lightColor = Color.BLUE
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
        notificationChannel.description = channelDescription

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun showNotification(context: Context, place : Place, title: String, msg: String) {
        val notificationId = notificationIdCounter++
        val intent = Intent(context, PlacesActivity::class.java)
        intent.putExtra("place", place)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_placelist_vector)
            .setColor(context.resources.getColor(R.color.red, null))
            .setContentTitle(title)
            .setContentText(msg)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
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