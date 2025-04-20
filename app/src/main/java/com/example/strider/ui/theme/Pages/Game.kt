package com.example.strider.ui.theme.Pages
import DataClass.Player
import ViewModels.ImageViewModel
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.example.strider.ui.theme.gradientPrimaryColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.width
import androidx.compose.ui.layout.ContentScale
import com.example.strider.ui.theme.BricolageGrotesque
import com.example.strider.ui.theme.MartianMono


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    imageViewModel: ImageViewModel?,
    roomCode: String,
    playerId: Int,
    startTime: Long,
    modifier:Modifier = Modifier,
    onPauseClicked: (roomCode: String, playerId: Int, startTime: Long) -> Unit,
    pictureProfil : Bitmap?) {

    val elapsed = remember { mutableStateOf(0L) }

    val firestoreClient = remember { FirestoreClient() }
    PlayerManager.currentPlayer?.firestoreClient= firestoreClient
    val players = remember { mutableStateListOf<Pair<Int, Player>>() }

    var distanceTotale = remember { mutableFloatStateOf(0f) }

    val isDarkTheme = isSystemInDarkTheme()
    val backgroundImageRes = if (isDarkTheme) R.drawable.wavy_game_dark else R.drawable.wavy_game
    val backgroundColor = if (isDarkTheme) Color(0xFF252525) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black


    LaunchedEffect(Unit) {
        firestoreClient.setHostLaunchGame(roomCode, true)

        while (true) {
            elapsed.value = (System.currentTimeMillis() - startTime)
            Log.d("TIMER", "Temps écoulé : ${elapsed.value / 1000}s")
            delay(1000)
        }
    }

    LaunchedEffect(roomCode) {
        firestoreClient.getPlayersInRoom(roomCode).collect { newPlayers ->
            players.clear()
            players.addAll(newPlayers)

            newPlayers.forEach { (id, player) ->
                if(player.distance.value> distanceTotale.value){distanceTotale.value= player.distance.value}
            }
        }
    }

    LaunchedEffect(playerId, players) {
        while (true) {
            val player = players.find { it.first == playerId }?.second
            val lastTimestamp = player?.timedDistance?.lastOrNull()?.second ?: System.currentTimeMillis()
            elapsed.value = lastTimestamp - startTime
            delay(1000)
        }
    }

    val minutes = (elapsed.value / 60000).toInt()
    val seconds = ((elapsed.value / 1000) % 60).toInt()
    val chronoText = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 50.dp),
        contentAlignment = (Alignment.BottomCenter)
    ) {

        LocationScreen(
            context = LocalContext.current,
        )
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .zIndex(-2f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ){
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            textAlign = TextAlign.Center,
            maxLines = 1,
            text = chronoText,
            fontSize = 90.sp,
            lineHeight = 116.sp,
            style = TextStyle(
                fontFamily = BricolageGrotesque,
                fontWeight = FontWeight.Bold
            ),
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

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(550.dp)
        ) {
            Image(
                painter = painterResource(id = if (isSystemInDarkTheme()) R.drawable.wavy_game_dark else R.drawable.wavy_game),
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
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {

                    itemsIndexed(players, key = { index, _ -> index }) { _, (id, player) ->
                        PlayerScoreStat(
                            player.distance.value,
                            imageViewModel = imageViewModel,
                            distanceMax = distanceTotale.value + 10
                        )

                    }

            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 50.dp),
            contentAlignment = (Alignment.BottomCenter)
        ) {
            Button(
                onClick = {onPauseClicked(roomCode, playerId, startTime) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                border = BorderStroke(2.dp, Color.White),
                modifier = Modifier
                    .width(300.dp)
                    .height(56.dp),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(23.dp),
            ) {
                Text("Statistics", fontFamily = MartianMono, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun PlayerScoreStat(
    distance: Float,
    distanceMax: Float,
    imageViewModel: ImageViewModel?,
    modifier: Modifier = Modifier,
    isHost: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .height(600.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        ProfilePicture(
            modifier = Modifier
                .padding(bottom = 10.dp)
                .size(50.dp)
                .clip(CircleShape),
            imageViewModel = imageViewModel,
            isHost = isHost
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10))
                .fillMaxHeight(distance / distanceMax)
                .background(MaterialTheme.colorScheme.secondary)
        ) {
            Text(
                text = String.format("%.1f", distance),
                style = typography.bodySmall.copy(color = Color.Red),
                modifier = Modifier
                    .padding(8.dp)
                    .graphicsLayer {
                        rotationZ = 90f
                    }
            )
        }
    }
}

@Composable
fun PlayerHorizontalBar(players: List<DataClass.Player>, modifier: Modifier) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 70.dp, end = 0.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        Column(
            modifier = Modifier
                .padding(5.dp),
            horizontalAlignment = Alignment.End,
        ) {
            for (player in players) {
                PlayerIconWithPseudo(player)
            }
        }
    }

}
@Composable
fun PlayerIconWithPseudo(player: DataClass.Player) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)

    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.beaute),
                contentDescription = "Player Icon",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            //Spacer(modifier = Modifier.height(-15.dp))
            Text(
                text = player.pseudo,
                style = TextStyle(color = colorScheme.onPrimaryContainer, fontSize = 16.sp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewMainScreen(){
    val testplayer = DataClass.Player( 1,"fec",  false,mutableListOf<Location>(
        Location("provider").apply {
            latitude = 40.7128 // Example: New York City
            longitude = -74.0060
            accuracy = 10f
        },
        Location("provider").apply {
            latitude = 34.0522 // Example: Los Angeles
            longitude = -118.2437
            accuracy = 15f
        },
        Location("provider").apply {
            latitude = 51.5074 // Example: London
            longitude = -0.1278
            accuracy = 12f
        }))
    StriderTheme {
        GameScreen(
            imageViewModel = null,
            roomCode = "",
            playerId = 0,
            startTime = System.currentTimeMillis(),
            onPauseClicked = { _, _, _ -> },
            pictureProfil = null
        )
    }
}