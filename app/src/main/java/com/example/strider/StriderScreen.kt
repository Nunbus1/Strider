package com.example.strider

import ViewModels.ImageViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.strider.ui.theme.Pages.*

/**
 * Contient les routes disponibles dans l'application.
 */
object StriderDestinations {
    const val ACCUEIL = "Accueil"
    const val CREATE = "Create/{pseudo}"
    const val LOBBY = "Lobby/{roomCode}/{playerId}"
    const val GAME = "Game/{roomCode}/{playerId}/{startTime}"
    const val FINISH = "Finish/{roomCode}/{playerId}/{startTime}"
}

/**
 * Composable racine qui gère la navigation entre les différentes pages de l’application Strider.
 *
 * @param navController Le contrôleur de navigation utilisé (par défaut : créé automatiquement).
 * @param imageViewModel Le ViewModel partagé entre les écrans pour gérer la photo de profil.
 */
@Composable
fun StriderApp(
    navController: NavHostController = rememberNavController(),
    imageViewModel: ImageViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = StriderDestinations.ACCUEIL,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // Accueil
            composable(route = StriderDestinations.ACCUEIL) {
                AccueilScreen(
                    imageViewModel = imageViewModel,
                    onJoinClicked = { roomCode, playerId ->
                        navController.navigate("Lobby/$roomCode/$playerId")
                    },
                    onCreateClicked = { pseudo ->
                        navController.navigate("Create/$pseudo")
                    }
                )
            }

            // Création de room
            composable(
                route = StriderDestinations.CREATE,
                arguments = listOf(navArgument("pseudo") { type = NavType.StringType })
            ) { backStackEntry ->
                val pseudo = backStackEntry.arguments?.getString("pseudo") ?: "Invité"
                CreateScreen(
                    imageViewModel = imageViewModel,
                    pseudo = pseudo,
                    onBackClicked = {
                        navController.navigate(StriderDestinations.ACCUEIL)
                    },
                    onCreateClicked = { roomCode, playerId ->
                        navController.navigate("Lobby/$roomCode/$playerId")
                    }
                )
            }

            // Lobby
            composable(
                route = StriderDestinations.LOBBY,
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
                    onBackClicked = {
                        navController.navigate(StriderDestinations.ACCUEIL)
                    },
                    onStartClicked = { code, id, startTime ->
                        navController.navigate("Game/$code/$id/$startTime")
                    }
                )
            }

            // Partie en cours
            composable(
                route = StriderDestinations.GAME,
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
                    }
                )
            }

            // Résultats
            composable(
                route = StriderDestinations.FINISH,
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
                        navController.popBackStack(StriderDestinations.ACCUEIL, inclusive = false)
                    }
                )
            }
        }
    }
}