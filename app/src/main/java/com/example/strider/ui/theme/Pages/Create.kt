package com.example.strider.ui.theme.Pages

import ViewModels.ImageViewModel
import android.location.Location
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.strider.ui.theme.gradientPrimaryColors
import com.example.strider.ui.theme.StriderTheme
import com.google.android.gms.location.LocationResult


@Composable
fun CreateScreen( imageViewModel: ImageViewModel?,
                  player : DataClass.Player,
                  onBackClicked: () -> Unit,
                  onCreateClicked: () -> Unit,
                  modifier: Modifier = Modifier
) {
    var description by remember { mutableStateOf("dada") }
    player.isHost=true
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Titre avec bouton retour

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
            ProfilePicture(modifier = Modifier.size(75.dp).clip(CircleShape), imageViewModel = imageViewModel)

        }

        Spacer(modifier = Modifier.height(32.dp))

        // Encadré "Lobby"
        Card(
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(2.dp, Color.Blue),
            modifier = Modifier.padding(horizontal = 32.dp)

        ) {
            Text(
                text = "Menu",
                fontSize = 40.sp,
                color = Color.Blue,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Séparation avec "Runners"
        Divider(color = Color.Gray, thickness = 1.dp)
        Text(
            text = "Settings",
            fontSize = 50.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Divider(color = Color.Gray, thickness = 1.dp)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "GameMode",
            fontSize = 20.sp,
            modifier = Modifier.padding(8.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { },
                modifier = Modifier,
                colors = ButtonDefaults.buttonColors(
                    Color.Transparent
                ),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(23.dp),
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(gradientPrimaryColors)
                        ).padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("<")
                }

            }
            Card(
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(2.dp, Color.Blue),
                modifier = Modifier.padding(horizontal = 32.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = "mode de jeu",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Button(
                onClick = { },
                modifier = Modifier,
                colors = ButtonDefaults.buttonColors(
                    Color.Transparent
                ),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(23.dp),
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(gradientPrimaryColors)
                        ).padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(">")
                }

            }
        }


        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.padding(10.dp)
                .fillMaxWidth(0.7f)
        ) {
            Text(
                text = "description mode de jeu",
                modifier = Modifier.padding(10.dp),
            )
        }
        Spacer(modifier = Modifier.height(2000.dp))
    }
                    // Bouton Start
    Box(
        modifier = Modifier.fillMaxSize()
            .padding(bottom = 50.dp),
        contentAlignment = (Alignment.BottomCenter)
        ,
        )
    {
        Button(
            onClick = onCreateClicked,
            modifier = Modifier.fillMaxWidth(0.7f)
                .align(Alignment.BottomCenter)
                    //.padding(15.dp,15.dp)
                .shadow(8.dp, shape = RoundedCornerShape(23.dp))
                ,
            colors = ButtonDefaults.buttonColors(
                Color.Transparent
            ),
            contentPadding = PaddingValues(),
            shape = RoundedCornerShape(23.dp),
            )
        {
            Box(modifier = Modifier.fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(gradientPrimaryColors))
                .padding(20.dp)

                ,
                contentAlignment = Alignment.Center
            )
            {
                Text("Create")
            }
            }
    }

}



@Preview(showBackground = true)
    //device="spec:width=1344dp,height=2992dp,dpi=489")
@Composable
fun CreateScreenPreview() {
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
        }),0f)
    StriderTheme {


        CreateScreen(
            imageViewModel = null,
            player = testplayer,
            onBackClicked = {},
            onCreateClicked = {}
        )
    }
}
