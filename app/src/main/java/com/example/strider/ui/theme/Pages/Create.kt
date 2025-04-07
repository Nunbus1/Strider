package com.example.strider.ui.theme.Pages

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.strider.ui.theme.gradientPrimaryColors
import kotlinx.coroutines.launch
import DataClass.Player
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun CreateScreen(
    pseudo: String,
    onBackClicked: () -> Unit,
    onCreateClicked: (roomCode: String, playerId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var description by remember { mutableStateOf("dada") }
    var roomCode by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val firestoreClient = FirestoreClient()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
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
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onBackClicked() }
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

        // Titre
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

        HorizontalDivider(color = Color.Gray, thickness = 1.dp)
        Text(
            text = "Settings",
            fontSize = 50.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        HorizontalDivider(color = Color.Gray, thickness = 1.dp)

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "GameMode", fontSize = 20.sp, modifier = Modifier.padding(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(23.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Brush.linearGradient(gradientPrimaryColors))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("<")
                }
            }

            Card(
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(2.dp, Color.Blue),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text(
                    text = "mode de jeu",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(23.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Brush.linearGradient(gradientPrimaryColors))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(">")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(0.7f)
        ) {
            Text(
                text = "description mode de jeu",
                modifier = Modifier.padding(10.dp),
            )
        }

        Spacer(modifier = Modifier.height(100.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    roomCode = generateUniqueRoomCode()
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .shadow(8.dp, shape = RoundedCornerShape(23.dp)),
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            contentPadding = PaddingValues(),
            shape = RoundedCornerShape(23.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(gradientPrimaryColors))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (roomCode.isNotEmpty()) roomCode else "Générer un code",
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))

        // 🔹 2. Créer la Room avec le code affiché
        Button(
            onClick = {
                if (roomCode.isNotEmpty()) {
                    val hostPlayer = Player(
                        pseudo = pseudo,
                        iconUrl = 1,
                        isHost = true
                    )

                    coroutineScope.launch {
                        firestoreClient.insertRoomWithHost(roomCode, hostPlayer).collect { result ->
                            if (result != null) {
                                Toast.makeText(context, "Room créée avec le code : $roomCode", Toast.LENGTH_SHORT).show()
                                onCreateClicked(roomCode, 0)
                            } else {
                                Toast.makeText(context, "Erreur lors de la création", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "Génère un code d'abord 😅", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .shadow(8.dp, shape = RoundedCornerShape(23.dp)),
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            contentPadding = PaddingValues(),
            shape = RoundedCornerShape(23.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(gradientPrimaryColors))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Créer la Room", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(2000.dp))
    }
}

suspend fun generateUniqueRoomCode(): String {
    val db = FirebaseFirestore.getInstance()
    var roomCode: String
    var isUnique: Boolean

    do {
        roomCode = generateRandomCode(6)
        val querySnapshot = db.collection("rooms")
            .whereEqualTo("code", roomCode)
            .get()
            .await()
        isUnique = querySnapshot.isEmpty
    } while (!isUnique)

    return roomCode
}

// 🔧 Génère un code de room aléatoire
fun generateRandomCode(length: Int): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}

@Preview(showBackground = true, device = "spec:width=1344dp,height=2992dp,dpi=489")
@Composable
fun CreateScreenPreview() {
    CreateScreen(
        pseudo = "" ,
        onBackClicked = {},
        onCreateClicked = { _, _ -> }
    )
}
