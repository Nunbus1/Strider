package com.example.strider.ui.theme.Pages

import DataClass.Player
import ViewModels.ImageViewModel
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.strider.LocationScreen
import com.example.strider.PlayerManager
import com.example.strider.R
import com.example.strider.ui.theme.StriderTheme
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.layout.ContentScale
import com.example.strider.IdManager
import com.example.strider.ui.theme.BricolageGrotesque
import com.example.strider.ui.theme.MartianMono


/**
 * Composable qui affiche l'écran principal de jeu.
 * Montre un chronomètre en temps réel, les distances parcourues par les joueurs,
 * et un bouton pour afficher les statistiques (pause).
 *
 * @param imageViewModel ViewModel contenant l'image du joueur actuel.
 * @param roomCode Code de la room Firebase.
 * @param playerId Identifiant du joueur actuel.
 * @param startTime Timestamp de départ de la course.
 * @param modifier Modificateur pour styliser le layout global.
 * @param onPauseClicked Fonction appelée pour naviguer vers les statistiques ou mettre en pause.
 */
@SuppressLint("DefaultLocale")
@Composable
fun GameScreen(
    imageViewModel: ImageViewModel?,
    roomCode: String,
    playerId: Int,
    startTime: Long,
    modifier: Modifier = Modifier,
    onPauseClicked: (roomCode: String, playerId: Int, startTime: Long) -> Unit,
) {
    // -------------------------
    // État et données Firestore
    // -------------------------

    val elapsed = remember { mutableLongStateOf(0L) }
    val firestoreClient = remember { FirestoreClient() }
    PlayerManager.currentPlayer?.firestoreClient = firestoreClient
    val players = remember { mutableStateListOf<Pair<Int, Player>>() }
    val distanceTotale = remember { mutableFloatStateOf(0f) }

    // -------------------------
    // Thème
    // -------------------------

    val isDarkTheme = isSystemInDarkTheme()
    val backgroundImageRes = if (isDarkTheme) R.drawable.wavy_game_dark else R.drawable.wavy_game
    val backgroundColor = if (isDarkTheme) Color(0xFF252525) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black

    // -------------------------
    // Chronomètre principal (affiché)
    // -------------------------

    LaunchedEffect(Unit) {
        firestoreClient.setHostLaunchGame(roomCode)

        while (true) {
            elapsed.longValue = (System.currentTimeMillis() - startTime)
            Log.d("TIMER", "Temps écoulé : ${elapsed.longValue / 1000}s")
            delay(1000)
        }
    }

    // -------------------------
    // Récupération continue des joueurs dans la room
    // -------------------------

    LaunchedEffect(roomCode) {
        firestoreClient.getPlayersInRoom(roomCode).collect { newPlayers ->
            players.clear()
            players.addAll(newPlayers)

            newPlayers.forEach { (_, player) ->
                if (player.distance.floatValue > distanceTotale.floatValue) {
                    distanceTotale.floatValue = player.distance.floatValue
                }
            }
        }
    }

    // -------------------------
    // Mise à jour du timer basé sur la dernière activité du joueur
    // -------------------------

    LaunchedEffect(playerId, players) {
        while (true) {
            val player = players.find { it.first == playerId }?.second
            val lastTimestamp = player?.timedDistance?.lastOrNull()?.second ?: System.currentTimeMillis()
            elapsed.longValue = lastTimestamp - startTime
            delay(1000)
        }
    }

    // -------------------------
    // Formatage du chrono
    // -------------------------

    val minutes = (elapsed.longValue / 60000).toInt()
    val seconds = ((elapsed.longValue / 1000) % 60).toInt()
    val chronoText = String.format("%02d:%02d", minutes, seconds)


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 50.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        LocationScreen(context = LocalContext.current)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .zIndex(-2f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = chronoText,
            textAlign = TextAlign.Center,
            fontSize = 60.sp,
            lineHeight = 116.sp,
            maxLines = 1,
            style = TextStyle(fontFamily = BricolageGrotesque, fontWeight = FontWeight.Bold),
            color = colorScheme.onBackground,
            modifier = Modifier.padding(8.dp)
        )

        Text(
            text = "Code : $roomCode",
            fontSize = 20.sp,
            fontFamily = MartianMono,
            color = textColor,
            modifier = Modifier.padding(8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(550.dp)
        ) {
            Image(
                painter = painterResource(id = backgroundImageRes),
                contentDescription = "Fond vague",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(-1f)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                itemsIndexed(players, key = { index, _ -> index }) { _, (id, player) ->
                    if (id == IdManager.currentPlayerId) {
                        PlayerScoreStat(
                            distance = player.distance.floatValue,
                            imageViewModel = imageViewModel,
                            distanceMax = distanceTotale.floatValue + 10,
                            isCurrentPlayer = true,
                            pseudo = "you",
                            textColor = colorScheme.secondary
                        )
                    } else {
                        PlayerScoreStat(
                            distance = player.distance.floatValue,
                            imageViewModel = null,
                            distanceMax = distanceTotale.floatValue + 10,
                            pseudo = player.pseudo,
                            textColor = textColor
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 50.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = { onPauseClicked(roomCode, playerId, startTime) },
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary),
                border = BorderStroke(2.dp, Color.White),
                modifier = Modifier
                    .width(300.dp)
                    .height(56.dp),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(23.dp)
            ) {
                Text(
                    "Statistics",
                    fontFamily = MartianMono,
                    color = colorScheme.primary
                )
            }
        }
    }
}


/**
 * Composable qui affiche une statistique de joueur sous forme de barre verticale.
 * La barre représente la distance parcourue relative à la distance maximale atteinte.
 * Si le joueur courant est affiché, un cercle blanc avec une bordure noire l'entoure.
 *
 * @param distance Distance actuelle du joueur.
 * @param distanceMax Distance maximale parmi tous les joueurs.
 * @param imageViewModel ViewModel contenant la photo du joueur (ou null pour un joueur autre).
 * @param modifier Modificateur à appliquer sur l’élément.
 * @param pseudo Nom du joueur affiché.
 * @param isCurrentPlayer Indique si ce joueur est celui de l'utilisateur local.
 * @param textColor Couleur du texte (adaptée au thème clair/sombre).
 */
@SuppressLint("DefaultLocale")
@Composable
fun PlayerScoreStat(
    distance: Float,
    distanceMax: Float,
    imageViewModel: ImageViewModel?,
    modifier: Modifier = Modifier,
    pseudo: String = "",
    isCurrentPlayer: Boolean = false,
    textColor: Color
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .height(600.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {

        Box(modifier = Modifier.size(50.dp)) {
            if (isCurrentPlayer) {
                Canvas(
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.Center)
                        .absoluteOffset(x = (-3).dp, y = (-3).dp)
                ) {
                    drawCircle(
                        color = Color.Black,
                        radius = size.minDimension / 1.9f,
                        center = center
                    )
                    drawCircle(
                        color = Color.White,
                        radius = size.minDimension / 2,
                        center = center
                    )
                }
            }

            ProfilePicture(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .size(50.dp)
                    .clip(CircleShape)
                    .align(Alignment.Center),
                imageViewModel = imageViewModel
            )
        }

        Text(
            text = pseudo.take(5),
            color = textColor,
            fontFamily = MartianMono,
            modifier = Modifier.padding(8.dp)
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10))
                .fillMaxHeight(distance / distanceMax)
                .background(colorScheme.secondary)
        ) {
            Text(
                text = String.format("%.1f", distance),
                color = textColor,
                fontFamily = MartianMono,
                modifier = Modifier
                    .padding(8.dp)
                    .graphicsLayer { rotationZ = 90f }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen(){
    
    StriderTheme {
        GameScreen(
            imageViewModel = null,
            roomCode = "chg",
            playerId = 0,
            startTime = System.currentTimeMillis(),
            onPauseClicked = { _, _, _ -> },


        )
    }
}