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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.strider.DataClass.Player
import ViewModels.ImageViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.strider.IdManager
import com.example.strider.PlayerManager
import com.example.strider.R
import com.example.strider.ui.theme.BricolageGrotesque
import com.example.strider.ui.theme.MartianMono
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

/**
 * Composable qui affiche l'écran de création de room.
 * L'utilisateur peut y entrer un code personnalisé ou générer un code aléatoire,
 * puis créer une nouvelle room dans laquelle il sera automatiquement l'hôte.
 *
 * @param imageViewModel ViewModel contenant la photo de profil de l'utilisateur.
 * @param pseudo Pseudo du joueur courant, utilisé lors de la création de la room.
 * @param onBackClicked Fonction appelée lorsqu'on clique sur le bouton de retour.
 * @param onCreateClicked Fonction appelée une fois la room créée, avec le code de room et l'identifiant joueur.
 * @param modifier Modificateur optionnel pour personnaliser la mise en page de l'écran.
 */
@Composable
fun CreateScreen(
    imageViewModel: ImageViewModel?,
    pseudo: String,
    onBackClicked: () -> Unit,
    onCreateClicked: (roomCode: String, playerId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // -------------------------
    // État local
    // -------------------------

    var roomCode by remember { mutableStateOf("") }

    // -------------------------
    // Contexte et Firebase
    // -------------------------

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val firestoreClient = FirestoreClient()

    // -------------------------
    // Gestion du thème
    // -------------------------

    val isDarkTheme = isSystemInDarkTheme()
    val backgroundRes = if (isDarkTheme) R.drawable.wavy_bot_dark else R.drawable.wavy_bot
    val backgroundColor = if (isDarkTheme) Color(0xFF252525) else Color.White
    val textColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary


    // -------------------------
    // Marquage du joueur actuel comme hôte
    // -------------------------

    PlayerManager.currentPlayer?.isHost = true

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
                style = TextStyle(
                    fontFamily = BricolageGrotesque,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = roomCode,
                onValueChange = { roomCode = it.uppercase().replace(" ", "") },
                label = { Text("Code") },
                placeholder = { Text("At least 6 characters") },
                singleLine = true,
                textStyle = TextStyle(fontFamily = MartianMono),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = MaterialTheme.colorScheme.secondary,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier.width(300.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val cleanedCode = roomCode.trim().replace("\\s".toRegex(), "")
                    if (cleanedCode.length < 6) {
                        Toast.makeText(context, "Code trop court 😅", Toast.LENGTH_SHORT).show()
                    } else {
                        coroutineScope.launch {
                            val exists = firestoreClient.checkIfRoomExists(cleanedCode)
                            if (exists) {
                                Toast.makeText(context, "Code déjà existant ❌", Toast.LENGTH_SHORT).show()
                            } else {
                                val hostPlayer = Player(pseudo = pseudo, iconUrl = 1, isHost = true)
                                firestoreClient.insertRoomWithHost(cleanedCode, hostPlayer).collect { result ->
                                    if (result != null) {
                                        Toast.makeText(context, "Room créée avec le code : $cleanedCode", Toast.LENGTH_SHORT).show()
                                        IdManager.currentRoomId = cleanedCode
                                        PlayerManager.currentPlayer?.firestoreClient = firestoreClient
                                        onCreateClicked(cleanedCode, 0)
                                    } else {
                                        Toast.makeText(context, "Erreur lors de la création", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                },
                contentPadding = PaddingValues(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                border = BorderStroke(2.dp, Color.White),
                modifier = Modifier
                    .width(300.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(23.dp)
            ) {
                Text(
                    "Créer la Room",
                    fontFamily = MartianMono,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        var newCode: String
                        do {
                            newCode = generateUniqueRoomCode()
                            val result = firestoreClient.insertRoomWithHost(
                                newCode,
                                Player(pseudo = pseudo, iconUrl = 1, isHost = true)
                            ).first()
                        } while (result == null)

                        roomCode = newCode
                        Toast.makeText(context, "Room créée avec le code : $newCode", Toast.LENGTH_SHORT).show()
                        IdManager.currentRoomId = newCode
                        PlayerManager.currentPlayer?.firestoreClient = firestoreClient
                        onCreateClicked(newCode, 0)
                    }
                },
                contentPadding = PaddingValues(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                border = BorderStroke(2.dp, Color.White),
                modifier = Modifier
                    .width(300.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(23.dp)
            ) {
                Text(
                    "Générer un code",
                    fontFamily = MartianMono,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


/**
 * Génère un code de room unique de 6 caractères,
 * en s'assurant qu'aucune room existante dans Firestore ne possède déjà ce code.
 */
suspend fun generateUniqueRoomCode(): String {

    // -------------------------
    // Références Firestore et état
    // -------------------------

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

/**
 * Génère un code de room aléatoire composé de lettres majuscules et de chiffres.
 *
 * @param length La longueur du code à générer.
 * @return Un code aléatoire de la longueur spécifiée.
 */
fun generateRandomCode(length: Int): String {

    // -------------------------
    // Liste des caractères possibles
    // -------------------------

    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    return (1..length)
        .map { chars.random() }
        .joinToString("")
}

@Preview(showBackground = true, device = "spec:width=1344dp,height=2992dp,dpi=489")
@Composable
fun CreateScreenPreview() {
    CreateScreen(
        imageViewModel = null,
        pseudo = "" ,
        onBackClicked = {},
        onCreateClicked = { _, _ -> }
    )
}
