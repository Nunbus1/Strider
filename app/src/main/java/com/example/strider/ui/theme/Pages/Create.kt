package com.example.strider.ui.theme.Pages

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.example.strider.R
import com.example.strider.ui.theme.gradientPrimaryColors
import com.example.strider.StriderScreen
import com.google.firebase.database.FirebaseDatabase
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch


@Composable
fun CreateScreen(
    onBackClicked: () -> Unit,
    onCreateClicked: () -> Unit,
    modifier: Modifier = Modifier
) {var description by remember { mutableStateOf("dada") }
    val coroutineScope = rememberCoroutineScope() // Création du scope
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
                    imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
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
                text = "Menu",
                fontSize = 40.sp,
                color = Color.Blue,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Séparation avec "Runners"
        HorizontalDivider(color = Color.Gray, thickness = 1.dp)
        Text(
            text = "Settings",
            fontSize = 50.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        HorizontalDivider(color = Color.Gray, thickness = 1.dp)

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
        Spacer(modifier = Modifier.height(100.dp))
        // Bouton Start
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(bottom = 50.dp),
            contentAlignment = (Alignment.BottomCenter)
            ,
        )
        {
            val firestoreClient = FirestoreClient()
            var room = Room(
                name = "test 2",
                code = generateRandomCode(6),
            )
            Button(onClick = {
                // Utilisation du scope Compose pour lancer la coroutine
                coroutineScope.launch {
                    firestoreClient.insertRoom(room).collect { id ->
                        room = room.copy(id = id ?: "Test")
                    }
                }
            },modifier = Modifier.fillMaxWidth(0.7f)
                .align(Alignment.BottomCenter)
                //.padding(15.dp,15.dp)
                .shadow(8.dp, shape = RoundedCornerShape(23.dp))
                ,
                colors = ButtonDefaults.buttonColors(
                    Color.Transparent
                ),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(23.dp),)

            {
                Box(modifier = Modifier.fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(gradientPrimaryColors))
                    .padding(20.dp)

                    ,
                    contentAlignment = Alignment.Center
                )
                {
                    Text(generateRandomCode(6))
                }
            }


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
fun createGameCode(database: FirebaseDatabase, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {

    val firestoreClient = FirestoreClient()

     var room = Room(
        name = "test 2",
        code = "test_2@gmail.com",
    )
    /*coroutineScope.launch {
        firestoreClient.insertRoom(room).collect { id ->
            room = room.copy(id = id ?: "")
        }
    }*/

}

fun generateRandomCode(length: Int): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}


@Preview(showBackground = true,
    device="spec:width=1344dp,height=2992dp,dpi=489")
@Composable
fun CreateScreenPreview() {
    CreateScreen(
        onBackClicked = {},
        onCreateClicked = {}
    )
}
