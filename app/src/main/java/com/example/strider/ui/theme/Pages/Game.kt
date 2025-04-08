package com.example.strider.ui.theme.Pages
import DataClass.Player
import ViewModels.ImageViewModel
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
import androidx.compose.runtime.mutableStateListOf
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    imageViewModel: ImageViewModel?,
    roomCode: String,
    playerId: Int,
    modifier:Modifier = Modifier,
    onPauseClicked: (roomCode: String, playerId: Int) -> Unit,
    pictureProfil : Bitmap?) {

    val firestoreClient = remember { FirestoreClient() }
    val players = remember { mutableStateListOf<Pair<Int, Player>>() }

    var distanceTotale = remember { mutableFloatStateOf(0f) }
    //var playerFlow = firestoreClient.getPlayerById(roomCode, playerId)
    //val myPlayer by playerFlow.collectAsState(initial = PlayerManager.currentPlayer)
    LaunchedEffect(roomCode) {
        firestoreClient.getPlayersInRoom(roomCode).collect { newPlayers ->
            players.clear()
            players.addAll(newPlayers)

            newPlayers.forEach { (id, player) ->
                Log.d("Debug", "Player[$id] = ${player.pseudo}")
                if(player.distance.value> distanceTotale.value){distanceTotale.value= player.distance.value}
            }
        }
    }
    //update distanceTotale based on the max distance in of every player in players
    //LaunchedEffect(players) {
    //    distanceTotale.value = players.maxOfOrNull { it.second.distance.value } ?: 0f
    //    Log.d("Debug", "distanceTotale = $distanceTotale")
    //}




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
    /*LaunchedEffect(roomCode) {
        firestoreClient.getPlayersInRoom(roomCode).collect { newPlayers ->
            players.clear()
            players.addAll(newPlayers)

            newPlayers.forEach { (id, player) ->
                Log.d("Debug", "Player[$id] = ${player.pseudo}")
            }
        }
    }*/

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.primary)
            .zIndex(-2f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TopAppBar(modifier = Modifier
            .background(brush = Brush.linearGradient(colors = gradientPrimaryColors))
            ,

            title = {
                Text(
                    modifier = modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {

                Icon(
                    modifier = Modifier.size(50.dp),
                    painter = painterResource(R.drawable.logo), // Use your desired icon
                    contentDescription = "Menu Icon",

                    )
            }

        )


        LazyRow(
            modifier = modifier
                .fillMaxSize(0.8f)

                .border(10.dp, color = Color.Black, shape = RoundedCornerShape(20.dp))
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(Color.Gray, shape = RoundedCornerShape(20.dp))

                .padding(18.dp),

            verticalAlignment = Alignment.Bottom, // Align children vertically to the center
            horizontalArrangement = Arrangement.SpaceEvenly,

            ) {
            //val distanceTotale by remember {  mutableStateOf(player.distance) }
            //create the first item with the current player


            itemsIndexed(players, key = { index, _ -> index }) { _, (id, player) ->
                PlayerScoreStat(
                    player.distance.value,
                    imageViewModel = imageViewModel,
                    distanceMax = distanceTotale.value + 10
                )
                Spacer(modifier = Modifier.weight(5f))
            }

        }



        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 50.dp),
            contentAlignment = (Alignment.BottomCenter)
        ) {
            Button(
                onClick = {onPauseClicked(roomCode, playerId) },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .shadow(8.dp, shape = RoundedCornerShape(23.dp)),
                colors = ButtonDefaults.buttonColors(
                    Color.Transparent
                ),

                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(23.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(gradientPrimaryColors)
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text("Pause")
                }

            }
        }
    }

    //PlayerHorizontalBar(players = ListePlayer, modifier = Modifier)
}

@Composable
fun PlayerScoreStat(distance: Float, distanceMax: Float,imageViewModel: ImageViewModel?, modifier: Modifier = Modifier,isHost: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .height(600.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,

        ) {

        ProfilePicture(modifier= Modifier
            .padding(bottom = 10.dp)
            .size(50.dp)
            .clip(CircleShape)
            , imageViewModel = imageViewModel,isHost= isHost)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10))

                .fillMaxHeight(distance / distanceMax)
                .background(
                    brush = Brush.linearGradient(colors = gradientPrimaryColors),
                ),
            //contentAlignment = Alignment.BottomCenter,
        ) {

            Text(
                text = distance.toString(),
                style = typography.bodySmall,
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
                //.background(color = colorScheme.secondary,
                //    shape = MaterialTheme.shapes.medium)
                .padding(5.dp),
            //.shadow(10.dp,shape = MaterialTheme.shapes.medium))
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
    val testplayer = DataClass.Player( 1,"fec",false,  mutableListOf<Location>(
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
            onPauseClicked = { _, _ -> },
            pictureProfil = null
        )
    }
}