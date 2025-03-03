package fr.isen.guerrand.isensmartcompanion.navigation
import fr.isen.guerrand.isensmartcompanion.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.compose.ui.platform.LocalContext
import fr.isen.guerrand.isensmartcompanion.ui.theme.*


// ✅ Barre de navigation
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        "Home" to R.drawable.baseline_home_24,
        "Events" to R.drawable.baseline_event_24,
        "Calendar" to R.drawable.calendar,
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
        composable("Calendar") { AgendaScreen() }
        composable("History") { HistoryScreen() }
        composable("EventDetail/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            val selectedEvent = loadEventsFromJson(context).find { it.id == eventId }
            EventDetailScreen(event = selectedEvent, navController = navController)
        }
    }
}