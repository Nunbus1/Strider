package com.example.strider

import ViewModels.ImageViewModel
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
fun StriderApp(navController: NavHostController = rememberNavController(), imageViewModel : ImageViewModel) {
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
                    imageViewModel = imageViewModel,
                    onJoinClicked = { roomCode: String, playerId: Int ->
                        navController.navigate("Lobby/$roomCode/$playerId")
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
                    imageViewModel = imageViewModel,
                    pseudo = pseudo,
                    onBackClicked = { navController.navigate(StriderScreen.Accueil.name) },
                    onCreateClicked = { roomCode: String, playerId: Int ->
                        navController.navigate("Lobby/$roomCode/$playerId")
                    }
                )
            }

            // Lobby
            composable(
                route = "Lobby/{roomCode}/{playerId}",
                arguments = listOf(
                    navArgument("roomCode") { type = NavType.StringType },
                    navArgument("playerId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
                val playerId = backStackEntry.arguments?.getInt("playerId") ?: -1
                LobbyScreen(
                    imageViewModel = imageViewModel,
                    roomCode = roomCode,
                    playerId = playerId,
                    onBackClicked = { navController.navigate(StriderScreen.Accueil.name) },
                    onStartClicked = { roomCode: String, playerId: Int, startTime: Long ->
                        navController.navigate("Game/$roomCode/$playerId/$startTime")

                    }
                )
            }

            composable(
                route = "Game/{roomCode}/{playerId}/{startTime}",
                arguments = listOf(
                    navArgument("roomCode") { type = NavType.StringType },
                    navArgument("playerId") { type = NavType.IntType },
                    navArgument("startTime") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
                val playerId = backStackEntry.arguments?.getInt("playerId") ?: -1
                val startTime = backStackEntry.arguments?.getLong("startTime") ?: System.currentTimeMillis()

                GameScreen(
                    imageViewModel = imageViewModel,
                    roomCode = roomCode,
                    playerId = playerId,
                    startTime = startTime,
                    onPauseClicked = { code, id, start ->
                        navController.navigate("Finish/$code/$id/$start")
                    },
                    pictureProfil = null
                )
            }

            // Finish
            composable(
                route = "Finish/{roomCode}/{playerId}/{startTime}",
                arguments = listOf(
                    navArgument("roomCode") { type = NavType.StringType },
                    navArgument("playerId") { type = NavType.IntType },
                    navArgument("startTime") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
                val playerId = backStackEntry.arguments?.getInt("playerId") ?: -1
                val startTime = backStackEntry.arguments?.getLong("startTime") ?: System.currentTimeMillis()

                FinishScreen(
                    imageViewModel = imageViewModel,
                    roomCode = roomCode,
                    playerId = playerId,
                    startTime = startTime,
                    onContinueClicked = { code, id, start ->
                        navController.navigate("Game/$code/$id/$start")
                    },
                    onHomeClicked = {
                        navController.navigate(StriderScreen.Accueil.name)
                    }
                )
            }
        }
    }
}
