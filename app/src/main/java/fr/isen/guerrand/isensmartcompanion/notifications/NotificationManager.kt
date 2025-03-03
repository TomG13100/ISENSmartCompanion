package fr.isen.guerrand.isensmartcompanion.notifications

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import fr.isen.guerrand.isensmartcompanion.R


fun scheduleNotification(context: Context, eventTitle: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
        Log.e("NotificationTest", "⛔ Exact alarms are not allowed. Requesting permission if necessary.")
        return
    }

    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("eventTitle", eventTitle)
        action = "fr.isen.guerrand.NOTIFY_EVENT"
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context, eventTitle.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val triggerTime = System.currentTimeMillis() + 10_000  // 10 seconds later

    Log.d("NotificationTest", "📅 Notification scheduled in 10s for: $eventTitle")

    try {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    } catch (e: SecurityException) {
        Log.e("NotificationTest", "❌ Failed to schedule exact alarm: ${e.message}")
    }
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val title = intent?.getStringExtra("eventTitle") ?: "Événement ISEN"
        Log.d("NotificationReceiver", "🔔 Notification reçue pour : $title")
        val notificationBuilder = NotificationCompat.Builder(context!!, "event_channel")
            .setSmallIcon(R.drawable.baseline_notifications_active_24)
            .setContentTitle("Rappel d'événement")
            .setContentText("Ne manquez pas : $title")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
        notificationManager?.notify(title.hashCode(), notificationBuilder.build())
    }
}

fun saveNotificationEvent(context: Context, eventId: String) {
    val prefs: SharedPreferences = context.getSharedPreferences("event_prefs", Context.MODE_PRIVATE)
    prefs.edit().putBoolean(eventId, true).apply()
}
