package com.example.strider

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.strider.ui.theme.Pages.*

enum class StriderScreen {
    Accueil,
    Create,
    Game,
    Lobby,
    Finish
}

@Composable
fun StriderApp(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = StriderScreen.valueOf(
        backStackEntry?.destination?.route?.substringBefore("/") ?: StriderScreen.Accueil.name
    )

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = StriderScreen.Accueil.name,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Accueil
            composable(route = StriderScreen.Accueil.name) {
                AccueilScreen(
                    onJoinClicked = { roomCode ->
                        navController.navigate("Lobby/$roomCode")
                    },
                    onCreateClicked = { pseudo ->
                        navController.navigate("Create/$pseudo")
                    }
                )
            }

            // Create
            composable(
                route = "Create/{pseudo}",
                arguments = listOf(navArgument("pseudo") { type = NavType.StringType })
            ) { backStackEntry ->
                val pseudo = backStackEntry.arguments?.getString("pseudo") ?: "Invité"
                CreateScreen(
                    pseudo = pseudo,
                    onBackClicked = { navController.navigate(StriderScreen.Accueil.name) },
                    onCreateClicked = { roomCode ->
                        navController.navigate("Lobby/$roomCode")
                    }
                )
            }

            // Lobby
            composable(
                route = "Lobby/{roomCode}",
                arguments = listOf(navArgument("roomCode") { type = NavType.StringType })
            ) { backStackEntry ->
                val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
                LobbyScreen(
                    roomCode = roomCode,
                    onBackClicked = { navController.navigate(StriderScreen.Accueil.name) },
                    onStartClicked = { navController.navigate(StriderScreen.Game.name) }
                )
            }

            // Game
            composable(route = StriderScreen.Game.name) {
                GameScreen(
                    onPauseClicked = { navController.navigate(StriderScreen.Finish.name) }
                )
            }

            // Finish
            composable(route = StriderScreen.Finish.name) {
                FinishScreen(
                    onContinueClicked = { navController.navigate(StriderScreen.Game.name) },
                    onHomeClicked = { navController.navigate(StriderScreen.Accueil.name) }
                )
            }
        }
    }
}
