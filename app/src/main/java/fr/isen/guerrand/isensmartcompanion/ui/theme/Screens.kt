package fr.isen.guerrand.isensmartcompanion.ui.theme

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import fr.isen.guerrand.isensmartcompanion.R
import fr.isen.guerrand.isensmartcompanion.data.*
import fr.isen.guerrand.isensmartcompanion.network.*
import fr.isen.guerrand.isensmartcompanion.notifications.*
import kotlinx.coroutines.launch
import retrofit2.Call

// ✅ Écran principal (Home)
@Composable
fun MainScreen() {

    var aiResponse by remember { mutableStateOf("Hello! How can I assist you today?") }
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { ChatDatabase.getDatabase(context) }
    val chatDao = remember { db.chatDao() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title and Logo
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_isen),
                contentDescription = "ISEN Logo",
                modifier = Modifier
                    .size(200.dp)

            )
            Text(
                text = "Smart Companion",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(y = (-60).dp)
            )

            Spacer(modifier = Modifier.height(16.dp))


        }

        Spacer(modifier = Modifier.height(32.dp))


        Text(
            text = aiResponse,
            fontSize = 18.sp,
            color = Color.DarkGray,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.Start)
                .padding(horizontal = 16.dp)


        )

        Spacer(modifier = Modifier.weight(1f))

        // Input Field and Send Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .offset(y = (-70).dp)
                .background(Color.LightGray, shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = userInput,
                onValueChange = { userInput = it },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
                cursorBrush = SolidColor(Color.Red),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (userInput.text.isEmpty()) {
                            Text(
                                text = "Ask a question",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Light,
                                color = Color(0xFF555555),
                                textAlign = TextAlign.Start
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Image(
                painter = painterResource(id = R.drawable.fleche),
                contentDescription = "Send",
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        Toast.makeText(context, "Question Submitted", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch {
                            aiResponse = GeminiApiService.getAiResponse(userInput.text)
                            chatDao.insertMessage(ChatMessage(question = userInput.text, answer = aiResponse))

                            userInput = TextFieldValue("")
                        }
                    },
                colorFilter = ColorFilter.tint(Color.Red)
            )
        }
    }
}


@Composable
fun EventsScreen(navController: NavHostController) {
    val events = remember { mutableStateOf<List<Event>>(emptyList()) }
    val retrofitService = RetrofitClient.instance
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // ✅ Charger les événements dès que l'écran est affiché
    LaunchedEffect(Unit) {
        retrofitService.getEvents().enqueue(object : retrofit2.Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: retrofit2.Response<List<Event>>) {
                if (response.isSuccessful) {
                    events.value = response.body() ?: emptyList()
                } else {
                    errorMessage.value = "Erreur serveur: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                errorMessage.value = "Impossible de récupérer les événements : ${t.message}"
            }
        })
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "ISEN Events",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // ✅ Affichage des erreurs si elles existent
        errorMessage.value?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp)
            )
        }

        // ✅ Affichage de la liste d'événements si aucun problème
        LazyColumn {
            items(events.value) { event ->
                EventItem(
                    event,
                    onClick = { navController.navigate("EventDetail/${event.id}") },
                    currentScreen = "Events"
                )
            }
        }
    }
}

@Composable
fun EventDetailScreen(event: Event?, navController: NavController) {
    val context = LocalContext.current
    var isSubscribed by remember { mutableStateOf(isEventSubscribed(context, event?.id ?: "")) }
    var isNotified by remember { mutableStateOf(isEventNotified(context, event?.id ?: "")) }


    if (event == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("❌ Aucun événement trouvé", fontSize = 20.sp, color = Color.Red)
            Button(onClick = { navController.popBackStack() }) {
                Text("Back")
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = event.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "📅 ${event.date}", fontSize = 18.sp)
            Text(text = "📍 ${event.location}", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = event.description, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(20.dp))


            Icon(
                painter = painterResource(id = if (isNotified) R.drawable.baseline_notifications_active_24 else R.drawable.baseline_notifications_off_24),
                contentDescription = "Notification",
                modifier = Modifier.size(40.dp).clickable {
                    saveNotificationEvent(context, event.id)
                    isNotified = !isNotified
                    if (isNotified) {
                        scheduleNotification(context, event.title)
                    }
                }
            )
            Spacer(modifier = Modifier.width(16.dp))

            // Toggle event subscription
            Icon(
                painter = painterResource(id = if (isSubscribed) R.drawable.baseline_check_24 else R.drawable.baseline_add_24),
                contentDescription = "Subscribe",
                modifier = Modifier.size(40.dp).clickable {
                    if (isSubscribed) {
                        removeUserEvent(context, event.id)
                    } else {
                        saveUserEvent(context, event.id)
                    }
                    isSubscribed = !isSubscribed
                }
            )

            Button(onClick = {
                navController.popBackStack()
            }) {
                Text("Back")
            }
        }
    }
}

@Composable
fun AgendaScreen() {
    val context = LocalContext.current
    val studentCourses = remember { mutableStateOf(loadStudentCourses()) }
    var events by remember { mutableStateOf(loadUserEvents(context)) }
    var eventToDelete by remember { mutableStateOf<Event?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val groupedEvents = (events + studentCourses.value).groupBy { it.date }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("📆 My Calendar", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

        if (groupedEvents.isEmpty()) {
            Text("No events scheduled.", fontSize = 16.sp, color = Color.Gray)
        } else {
            LazyColumn {
                groupedEvents.forEach { (date, items) ->
                    item {
                        Text(text = "📅 $date", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(items) { item ->
                        if (item is Event) {
                            EventItem(item, onClick = {
                                eventToDelete = item
                                showDialog = true
                            }, currentScreen = "Calendar")
                        } else if (item is StudentCourse) {
                            CourseItem(item)
                        }
                    }

                }
            }
        }
    }

    // Confirmation Dialog for Event Removal
    if (showDialog && eventToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Remove Event?") },
            text = { Text("Are you sure you want to remove this event from your calendar?") },
            confirmButton = {
                Button(onClick = {
                    eventToDelete?.let {
                        removeUserEvent(context, it.id)
                        events = loadUserEvents(context)
                    }
                    showDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun CourseItem(course: StudentCourse) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "📖 ${course.title}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "📅 ${course.date}", fontSize = 14.sp, color = Color.Gray)
            Text(text = "⏰ ${course.time}", fontSize = 14.sp, color = Color.Gray)
            Text(text = "📍 ${course.location}", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val db = remember { ChatDatabase.getDatabase(context) }
    val chatDao = remember { db.chatDao() }
    val coroutineScope = rememberCoroutineScope()

    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var messageToDelete by remember { mutableStateOf<ChatMessage?>(null) } // Stocke le message sélectionné pour suppression
    var showDialog by remember { mutableStateOf(false) }

    // Charger l'historique
    LaunchedEffect(Unit) {
        messages = chatDao.getAllMessages()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Conversations history",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(messages) { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            messageToDelete = message
                            showDialog = true // Affiche la boîte de dialogue
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "🗨️ ${message.question}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "🤖 ${message.answer}", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    chatDao.clearHistory()
                    messages = emptyList()
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Delete all history")
        }
    }

    // Afficher la boîte de dialogue de confirmation si nécessaire
    if (showDialog && messageToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete from history?") },
            text = { Text("Are you sure you want to delete this conversation?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            chatDao.deleteMessage(messageToDelete!!)
                            messages = chatDao.getAllMessages()
                        }
                        showDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text("No")
                }
            }
        )
    }
}

// ✅ Preview
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ISENSmartCompanionTheme {
        MainScreen()
    }
}