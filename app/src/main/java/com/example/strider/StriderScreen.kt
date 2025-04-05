package com.example.strider

import ViewModels.ImageViewModel
import androidx.annotation.StringRes
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.strider.ui.theme.Pages.GameScreen
import com.example.strider.ui.theme.Pages.CreateScreen
import com.example.strider.ui.theme.Pages.AccueilScreen
import com.example.strider.ui.theme.Pages.LobbyScreen
import com.example.strider.ui.theme.Pages.FinishScreen
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable



enum class StriderScreen() {
    Accueil,
    Create,
    Game,
    Lobby,
    Finish
}
@Composable
fun StriderApp(imageViewModel : ImageViewModel,
               player : DataClass.Player,
               context: Context,
               navController: NavHostController = rememberNavController()

) {
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = StriderScreen.valueOf(
        backStackEntry?.destination?.route ?: StriderScreen.Accueil.name
    )
Box(modifier = Modifier
    .fillMaxSize()){
    NavHost(
        navController = navController,
        startDestination = StriderScreen.Accueil.name,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        //Accueil
        composable(route = StriderScreen.Accueil.name) {
            val context = LocalContext.current
            AccueilScreen(
                imageViewModel = imageViewModel,
                player=player,
                onJoinClicked = { navController.navigate(StriderScreen.Lobby.name) },
                onCreateClicked = { navController.navigate(StriderScreen.Create.name) }
            )
        }
        //Create
        composable(route = StriderScreen.Create.name) {
            val context = LocalContext.current
            CreateScreen(
                imageViewModel = imageViewModel,
                player=player,
                onBackClicked = { navController.navigate(StriderScreen.Accueil.name) },
                onCreateClicked = { navController.navigate(StriderScreen.Lobby.name) }
            )
        }
        //Game
        composable(route = StriderScreen.Game.name) {
            val context = LocalContext.current
            GameScreen(
                imageViewModel = imageViewModel,
                player=player,
                onPauseClicked = {navController.navigate(StriderScreen.Finish.name)}, pictureProfil = null
            )
        }
        //Lobby
        composable(route = StriderScreen.Lobby.name) {
            val context = LocalContext.current
            LobbyScreen(
                imageViewModel = imageViewModel,
                player=player,
                onBackClicked = {navController.navigate(StriderScreen.Accueil.name)},
                onStartClicked = {navController.navigate(StriderScreen.Game.name)}
            )
        }
        composable(route = StriderScreen.Finish.name) {
            val context = LocalContext.current
            FinishScreen(
                imageViewModel = imageViewModel,
                player=player,
                onContinueClicked = { navController.navigate(StriderScreen.Game.name) },
                onHomeClicked = { navController.navigate(StriderScreen.Accueil.name) }
            )
        }
    }
}
}
