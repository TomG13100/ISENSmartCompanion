package fr.isen.guerrand.isensmartcompanion.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.isen.guerrand.isensmartcompanion.R
import fr.isen.guerrand.isensmartcompanion.data.Event


fun isEventSubscribed(context: Context, eventId: String): Boolean {
    val prefs: SharedPreferences = context.getSharedPreferences("user_events", Context.MODE_PRIVATE)
    val savedEventIds = prefs.getStringSet("saved_events", emptySet()) ?: emptySet()
    return eventId in savedEventIds
}

fun saveUserEvent(context: Context, eventId: String) {
    val prefs = context.getSharedPreferences("user_events", Context.MODE_PRIVATE)
    val eventsSet = prefs.getStringSet("saved_events", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    eventsSet.add(eventId)
    prefs.edit().putStringSet("saved_events", eventsSet).apply()
}

fun removeUserEvent(context: Context, eventId: String) {
    val prefs = context.getSharedPreferences("user_events", Context.MODE_PRIVATE)
    val eventsSet = prefs.getStringSet("saved_events", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    eventsSet.remove(eventId)
    prefs.edit().putStringSet("saved_events", eventsSet).apply()
}

fun loadUserEvents(context: Context): List<Event> {
    val prefs = context.getSharedPreferences("user_events", Context.MODE_PRIVATE)
    val savedEventIds = prefs.getStringSet("saved_events", emptySet()) ?: emptySet()
    return loadEventsFromJson(context).filter { it.id in savedEventIds }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

@Composable
fun EventItem(event: Event, onClick: () -> Unit, currentScreen: String) {
    val context = LocalContext.current
    var isSubscribed by remember { mutableStateOf(isEventSubscribed(context, event.id)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "📅 ${event.date}", fontSize = 14.sp, color = Color.Gray)
                Text(text = "📍 ${event.location}", fontSize = 14.sp, color = Color.Gray)
            }

            // ✅ Vérifier si l'icône doit être affichée
            if (currentScreen != "Calendar") {
                Icon(
                    painter = painterResource(id = if (isSubscribed) R.drawable.baseline_check_24 else R.drawable.baseline_add_24),
                    contentDescription = "Subscribe",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            if (isSubscribed) {
                                removeUserEvent(context, event.id)
                                showToast(context, "❌ Événement supprimé de l'agenda")
                            } else {
                                saveUserEvent(context, event.id)
                                showToast(context, "✅ Événement ajouté à l'agenda")
                            }
                            isSubscribed = !isSubscribed
                        }
                )
            }
        }
    }
}

fun isEventNotified(context: Context, eventId: String): Boolean {
    val prefs: SharedPreferences = context.getSharedPreferences("event_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean(eventId, false)
}

// ✅ Fonction pour charger les événements depuis le JSON
fun loadEventsFromJson(context: Context): List<Event> {
    val inputStream = context.resources.openRawResource(R.raw.events)
    val jsonString = inputStream.bufferedReader().use { it.readText() }

    val listType = object : TypeToken<List<Event>>() {}.type
    return Gson().fromJson(jsonString, listType)
}