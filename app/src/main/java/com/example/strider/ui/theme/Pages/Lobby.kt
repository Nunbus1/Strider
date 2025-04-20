package com.example.strider.ui.theme.Pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.strider.R
import com.google.firebase.firestore.FirebaseFirestore
import DataClass.Player
import ViewModels.ImageViewModel
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import com.example.strider.ui.theme.BricolageGrotesque
import com.example.strider.ui.theme.MartianMono
import kotlinx.coroutines.flow.collectLatest


@Composable
fun LobbyScreen(
    imageViewModel : ImageViewModel?,
    roomCode: String,
    playerId: Int,
    onBackClicked: () -> Unit,
    onStartClicked: (roomCode: String, playerId: Int, startTime: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val firestoreClient = remember { FirestoreClient() }
    val players = remember { mutableStateListOf<Pair<Int, Player>>() }

    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color(0xFF252525) else Color.White
    val backgroundRes = if (isDarkTheme) R.drawable.wave_dark else R.drawable.wave
    val textColor = if (isDarkTheme) Color.White else Color.Black

    LaunchedEffect(roomCode) {
        firestoreClient.getPlayersInRoom(roomCode).collect { newPlayers ->
            players.clear()
            players.addAll(newPlayers)

            newPlayers.forEach { (id, player) ->
                Log.d("Debug", "Player[$id] = ${player.pseudo}")
            }
        }
        firestoreClient.getHostLaunchGame(roomCode).collect{
            if(it == true){
                onStartClicked(roomCode, playerId, System.currentTimeMillis())
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        // Header
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
                imageViewModel = imageViewModel,
                isHost = true
            )
        }
        Text(
            text = "Strider",
            fontSize = 100.sp,
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
            fontSize = 80.sp,
            lineHeight = 116.sp,
            style = TextStyle(
                fontFamily = BricolageGrotesque,
                fontWeight = FontWeight.Bold
            ),
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

        Button(
            onClick = {
                val currentTime = System.currentTimeMillis()
                onStartClicked(roomCode, playerId, currentTime) // pour le host
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            border = BorderStroke(2.dp, Color.White),
            modifier = Modifier
                .width(300.dp)
                .height(56.dp)
        ) {
            Text("Start", fontFamily = MartianMono, color = MaterialTheme.colorScheme.primary)
        }


            Spacer(modifier = Modifier.height(16.dp))

    }
}

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
        Box(
            modifier = Modifier.size(60.dp)
        ) {
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

@Composable
fun ButtonStart(
    isHost: Boolean,
    onStartClicked: (roomCode: String, playerId: Int, startTime: Long) -> Unit,
    roomCode: String,
    playerId: Int,) {

    if(isHost){
        Button(
            onClick = {
                val currentTime = System.currentTimeMillis()
                onStartClicked(roomCode, playerId, currentTime) // pour le host
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF22A6FF),
                            Color(0xFF0044FF)
                        )
                    ),
                    shape = CircleShape
                )
                .width(150.dp)
        ) {
            Text("Start")
        }
    }
    else(
            Text(
                text = "Waiting for the hosting player...",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            )


    Spacer(modifier = Modifier.height(16.dp))

}

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
