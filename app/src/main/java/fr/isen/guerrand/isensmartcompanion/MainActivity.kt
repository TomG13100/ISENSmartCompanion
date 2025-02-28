package fr.isen.guerrand.isensmartcompanion

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.isen.guerrand.isensmartcompanion.ui.theme.ISENSmartCompanionTheme
import kotlinx.parcelize.Parcelize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import android.widget.Toast
import androidx.navigation.NavController
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


// ✅ Modèle de données pour un événement
@Parcelize
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val category: String
) : Parcelable

// ✅ Interface API Retrofit (Nouvelle version avec suspend fun)
interface EventApiService {
    @GET("events.json")
    fun getEvents(): Call<List<Event>>
}


// ✅ Création de l'instance Retrofit
object RetrofitClient {
    private const val BASE_URL = "https://isen-smart-companion-default-rtdb.europe-west1.firebasedatabase.app/"

    val instance: EventApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EventApiService::class.java)
    }
}




// ✅ Fonction pour charger les événements depuis le JSON
fun loadEventsFromJson(context: Context): List<Event> {
    val inputStream = context.resources.openRawResource(R.raw.events)
    val jsonString = inputStream.bufferedReader().use { it.readText() }

    val listType = object : TypeToken<List<Event>>() {}.type
    return Gson().fromJson(jsonString, listType)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ISENSmartCompanionTheme {
                MainApp()
            }
        }
    }
}

// ✅ Application principale avec barre de navigation
@Composable
fun MainApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavigationGraph(navController)
        }
    }
}

// ✅ Barre de navigation
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        "Home" to R.drawable.baseline_home_24,
        "Events" to R.drawable.baseline_event_24,
        "History" to R.drawable.history
    )

    NavigationBar {
        items.forEach { (screen, icon) ->
            NavigationBarItem(
                selected = false,
                onClick = { navController.navigate(screen) },
                label = { Text(screen) },
                icon = {
                    Image(
                        painter = painterResource(id = icon),
                        contentDescription = screen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        }
    }
}

// ✅ Navigation entre écrans
@Composable
fun NavigationGraph(navController: NavHostController) {
    val context = LocalContext.current

    NavHost(navController, startDestination = "Home") {
        composable("Home") { MainScreen() }
        composable("Events") { EventsScreen(navController) }
        composable("History") { HistoryScreen() }
        composable("EventDetail/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            val selectedEvent = loadEventsFromJson(context).find { it.id == eventId }
            EventDetailScreen(event = selectedEvent, navController = navController)
        }
    }
}

// ✅ Écran principal (Home)
@Composable
fun MainScreen() {

    var aiResponse by remember { mutableStateOf("Hello! How can I assist you today?") }
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    val context = LocalContext.current

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
                        aiResponse = userInput.text
                    },
                colorFilter = ColorFilter.tint(Color.Red)
            )
        }
    }
}

@Composable
fun EventsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val events = remember { mutableStateOf<List<Event>>(emptyList()) }
    val retrofitService = RetrofitClient.instance
    val errorMessage = remember { mutableStateOf<String?>(null) }  // Stocker l'erreur si besoin
    val coroutineScope = rememberCoroutineScope()

    // ✅ Charger les événements dès que l'écran est affiché
    LaunchedEffect(Unit) {
        retrofitService.getEvents().enqueue(object : retrofit2.Callback<List<Event>> {
            override fun onResponse(call: retrofit2.Call<List<Event>>, response: retrofit2.Response<List<Event>>) {
                if (response.isSuccessful) {
                    events.value = response.body() ?: emptyList()
                } else {
                    errorMessage.value = "Erreur serveur: ${response.code()}"
                }
            }

            override fun onFailure(call: retrofit2.Call<List<Event>>, t: Throwable) {
                errorMessage.value = "Impossible de récupérer les événements : ${t.message}"
            }
        })
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Événements ISEN",
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
                EventItem(event) {
                    navController.navigate("EventDetail/${event.id}")
                }
            }
        }
    }
}


// ✅ Affichage d'un événement
@Composable
fun EventItem(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "📅 ${event.date}", fontSize = 14.sp, color = Color.Gray)
            Text(text = "📍 ${event.location}", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun EventDetailScreen(event: Event?, navController: NavController) {
    println("⚠️ EventDetailScreen lancé avec event: $event")  // ✅ Ajoute ce log pour vérifier

    if (event == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("❌ Aucun événement trouvé", fontSize = 20.sp, color = Color.Red)
            Button(onClick = { navController.popBackStack() }) {
                Text("Retour")
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

            Button(onClick = {
                println("Retour button clicked")  // ✅ Log pour voir si ça fonctionne
                navController.popBackStack()
            }) {
                Text("Retour")
            }
        }
    }
}



// ✅ Écran History
@Composable
fun HistoryScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Historique", style = MaterialTheme.typography.headlineSmall)
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
