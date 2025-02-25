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

@Composable
fun LobbyScreen(
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
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.Gray, CircleShape)
            )
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

        Spacer(modifier = Modifier.height(48.dp))

        // Séparation avec "Runners"
        Divider(color = Color.Gray, thickness = 1.dp)
        Text(
            text = "Runners",
            fontSize = 50.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Divider(color = Color.Gray, thickness = 1.dp)

        Spacer(modifier = Modifier.height(32.dp))

        // Liste des joueurs
        val players = listOf(
            Pair(R.drawable.logo, "PlayerOne"),
            Pair(R.drawable.logo, "Speedster"),
            Pair(R.drawable.logo, "Shadow"),
            Pair(R.drawable.logo, "Blaze")
        )

        players.forEach { (imageRes, pseudo) ->
            PlayerCard(imageRes, pseudo)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bouton Start
        Button(
            onClick = onStartClicked,
            modifier = Modifier.fillMaxWidth(0.7f)
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
        Image(
            painter = painterResource(imageRes),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(25.dp))
        )
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
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LobbyScreenPreview() {
    LobbyScreen(
        onBackClicked = {},
        onStartClicked = {}
    )
}
