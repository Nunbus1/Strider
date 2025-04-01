package com.example.strider.ui.theme.Pages

import ViewModels.ImageViewModel
import android.location.Location
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
import com.example.strider.R
import com.google.android.gms.location.LocationResult

@Composable
fun LobbyScreen(
    imageViewModel: ImageViewModel?,
    player: DataClass.Player,
    onBackClicked: () -> Unit,
    onStartClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        // Titre avec bouton retour
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(32.dp).clickable { onBackClicked() }
                )
            }
            Text(
                text = "Strider",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold

            )
            ProfilePicture(modifier = Modifier.size(75.dp)
                .clip(CircleShape), imageViewModel = imageViewModel)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Encadré "Lobby"
        Card(
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(2.dp, Color.Blue),
            modifier = Modifier.padding(horizontal = 32.dp)

        ) {
            Text(
                text = "Lobby",
                fontSize = 40.sp,
                color = Color.Blue,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Code : A3FG9",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Séparation avec "Runners"
        Divider(color = Color.Gray, thickness = 1.dp)
        Text(
            text = "Runners",
            fontSize = 40.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Divider(color = Color.Gray, thickness = 1.dp)

        Spacer(modifier = Modifier.height(32.dp))

        // Liste des joueurs
        val players = listOf(
            //PLayer(R.drawable.beaute, "PlayerOne"),
            Pair(R.drawable.beaute, "Speedster"),
            Pair(R.drawable.beaute, "Shadow"),
            Pair(R.drawable.beaute, "Blaze")
        )
        PlayerCard(R.drawable.beaute, player.pseudo)
        Spacer(modifier = Modifier.height(8.dp))
        players.forEach { (imageRes, pseudo) ->
            PlayerCard(imageRes, pseudo)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bouton Start
        Button(
            onClick = onStartClicked,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(colors = listOf(Color(0xFF22A6FF), Color(0xFF0044FF))),
                    shape = CircleShape
                )
                .width(150.dp)
        ) {
            Text("Start")
        }
    }
}

@Composable
fun PlayerCard(imageRes: Int, pseudo: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfilePicture(modifier = Modifier.size(60.dp)
            .clip(CircleShape), imageViewModel = null)
        Spacer(modifier = Modifier.width(16.dp))
        Card(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .shadow(8.dp, shape = RoundedCornerShape(16.dp))
                .padding(5.dp)
        ) {
            Text(
                text = pseudo,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LobbyScreenPreview() {
    val testplayer = DataClass.Player( 1,"fec",false, LocationResult.create(listOf(
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
        })),0f)
    LobbyScreen(
        imageViewModel = null,
        player = testplayer,
        onBackClicked = {},
        onStartClicked = {}
    )
}
