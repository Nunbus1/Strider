package com.example.strider.ui.theme.Pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.strider.R
import com.example.strider.DataClass.Player
import ViewModels.ImageViewModel
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.zIndex
import com.example.strider.PlayerManager
import com.example.strider.ui.theme.BricolageGrotesque
import com.example.strider.ui.theme.MartianMono
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest


/**
 * Composable qui affiche l'écran de Lobby avant le début d'une partie.
 * Les joueurs présents sont listés et le joueur hôte peut lancer la partie après un compte à rebours.
 *
 * @param imageViewModel ViewModel contenant l'image de profil du joueur courant.
 * @param roomCode Code de la room dans Firestore.
 * @param playerId Identifiant du joueur courant.
 * @param onBackClicked Fonction appelée lors du retour arrière.
 * @param onStartClicked Fonction déclenchée pour démarrer la partie avec l'heure de départ.
 * @param modifier Modificateur pour styliser le layout principal.
 */
@Composable
fun LobbyScreen(
    imageViewModel: ImageViewModel?,
    roomCode: String,
    playerId: Int,
    onBackClicked: () -> Unit,
    onStartClicked: (roomCode: String, playerId: Int, startTime: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // -------------------------
    // Firestore et gestion des joueurs
    // -------------------------

    val firestoreClient = remember { FirestoreClient() }
    val players = remember { mutableStateListOf<Pair<Int, Player>>() }

    // -------------------------
    // Thème et ressources visuelles
    // -------------------------

    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color(0xFF252525) else Color.White
    val backgroundRes = if (isDarkTheme) R.drawable.wave_dark else R.drawable.wave
    val textColor = if (isDarkTheme) Color.White else Color.Black

    // -------------------------
    // État pour le compte à rebours
    // -------------------------

    val countdown = remember { mutableIntStateOf(5) }
    val showCountdown = remember { mutableStateOf(true) }
    val countdownStarted = remember { mutableStateOf(false) }
    val shouldStartCountdown = remember { mutableStateOf(false) }
    var hostLaunchGame by remember { mutableStateOf<Boolean?>(false) }

    // -------------------------
    // Effet : compte à rebours au lancement
    // -------------------------

    LaunchedEffect(shouldStartCountdown.value) {
        if (shouldStartCountdown.value && !countdownStarted.value) {
            countdownStarted.value = true
            showCountdown.value = true

            while (countdown.intValue > 0) {
                delay(1000)
                countdown.value -= 1
            }

            showCountdown.value = false
            val currentTime = System.currentTimeMillis()
            onStartClicked(roomCode, playerId, currentTime)
        }
    }

    // -------------------------
    // Effet : récupération des joueurs dans la room
    // -------------------------

    LaunchedEffect(roomCode) {
        firestoreClient.getPlayersInRoom(roomCode).collect { newPlayers ->
            players.clear()
            players.addAll(newPlayers)
            newPlayers.forEach { (id, player) ->
                Log.d("Debug", "Player[$id] = ${player.pseudo}")
            }
        }
    }

    // -------------------------
    // Effet : détection du lancement par l'hôte
    // -------------------------

    LaunchedEffect(hostLaunchGame) {
        firestoreClient.getHostLaunchGame(roomCode).collectLatest { value ->
            hostLaunchGame = value
        }
    }

    if (hostLaunchGame == true && !countdownStarted.value) {
        shouldStartCountdown.value = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onBackClicked() }
                )
            }

            ProfilePicture(
                modifier = Modifier
                    .size(50.dp)
                    .background(shape = CircleShape, color = Color.White),
                imageViewModel = imageViewModel
            )
        }

        Text(
            text = "Strider",
            fontSize = 60.sp,
            lineHeight = 116.sp,
            style = TextStyle(fontFamily = BricolageGrotesque, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(8.dp)
        )

        Text(
            text = "Code : $roomCode",
            fontSize = 20.sp,
            fontFamily = MartianMono,
            color = textColor,
            modifier = Modifier.padding(8.dp)
        )

        if (showCountdown.value) {
            Box(
                modifier = Modifier.fillMaxSize().zIndex(10f),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = countdown.intValue,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "CountdownTransition"
                ) { value ->
                    Text(
                        text = when (value) {
                            4, 3, 2 -> (value - 1).toString()
                            1 -> "Partez !"
                            else -> ""
                        },
                        fontSize = 50.sp,
                        fontFamily = MartianMono,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }

        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = "Séparateur décoratif",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
        )

        Text(
            text = "Runners",
            fontSize = 60.sp,
            lineHeight = 116.sp,
            style = TextStyle(fontFamily = BricolageGrotesque, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(8.dp)
        )

        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = "Séparateur décoratif",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(players, key = { index, _ -> index }) { _, (id, player) ->
                PlayerCard(
                    imageRes = getDrawableFromId(player.iconUrl),
                    pseudo = player.pseudo,
                    isHost = player.isHost,
                    isCurrentUser = id == playerId
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (PlayerManager.currentPlayer?.isHost == true) {
            Button(
                onClick = { shouldStartCountdown.value = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                border = BorderStroke(2.dp, Color.White),
                modifier = Modifier
                    .width(300.dp)
                    .height(56.dp)
            ) {
                Text(
                    "Start",
                    fontFamily = MartianMono,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}


/**
 * Composable qui affiche une carte de joueur dans la liste du lobby.
 * Montre l'avatar, le pseudo, et une couronne si le joueur est l'hôte.
 * Ajoute également "(You)" si c’est le joueur courant.
 *
 * @param imageRes Ressource drawable de l'image de profil du joueur.
 * @param pseudo Pseudo du joueur affiché.
 * @param isHost Indique si le joueur est l'hôte de la partie.
 * @param isCurrentUser Indique si ce joueur correspond à l'utilisateur actuel.
 */
@Composable
fun PlayerCard(
    imageRes: Int,
    pseudo: String,
    isHost: Boolean,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(60.dp)) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(25.dp))
            )
            if (isHost) {
                Text(
                    text = "👑",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Card(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .shadow(8.dp, shape = RoundedCornerShape(16.dp))
                .padding(5.dp)
        ) {
            Text(
                text = if (isCurrentUser) "$pseudo (You)" else pseudo,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

/**
 * Retourne l'identifiant drawable correspondant à une valeur d'icône.
 *
 * @param iconUrl Identifiant numérique de l'icône (1, 2, etc.).
 * @return Ressource drawable associée à l'identifiant.
 */
private fun getDrawableFromId(iconUrl: Int): Int {
    return when (iconUrl) {
        1 -> R.drawable.beaute
        else -> R.drawable.beaute
    }
}

@Preview(showBackground = true)
@Composable
fun LobbyScreenPreview() {
    LobbyScreen(
        imageViewModel = null,
        roomCode = "",
        playerId = 0,
        onBackClicked = {},
        onStartClicked = { _, _, _ -> }
    )
}
