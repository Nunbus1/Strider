package com.example.strider.ui.theme.Pages

import DataClass.Player
import ViewModels.ImageViewModel
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.strider.IdManager
import com.example.strider.R
import com.example.strider.ui.theme.BricolageGrotesque
import com.example.strider.ui.theme.MartianMono
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun AccueilScreen(
    imageViewModel : ImageViewModel?,
    onCreateClicked: (String) -> Unit,
    onJoinClicked: (roomCode: String, playerId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var pseudo by remember { mutableStateOf(TextFieldValue("")) }

    val placeholderText = "Enter your pseudo"

    var code by remember { mutableStateOf("") }
    var isJoining by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val firestoreClient = FirestoreClient()

    var joiningInProgress by remember { mutableStateOf(false) }

    val isDarkTheme = isSystemInDarkTheme()
    val backgroundRes = if (isDarkTheme) R.drawable.wavy_top_dark else R.drawable.wavy_top
    val backgroundColor = if (isDarkTheme) Color(0xFF252525) else Color.White

    // Gérer le bouton retour du téléphone
    BackHandler(isJoining) {
        isJoining = false
    }


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

            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            TakeProfilePicture(imageViewModel)

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = pseudo,
                onValueChange = { pseudo = it },
                placeholder = {
                    Text(text = placeholderText, fontFamily = MartianMono)
                },
                textStyle = TextStyle(fontFamily = MartianMono),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                    focusedTextColor = MaterialTheme.colorScheme.secondary,
                    unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                    cursorColor = MaterialTheme.colorScheme.secondary,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier.width(300.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onCreateClicked(pseudo.text) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier
                        .width(300.dp)
                        .height(56.dp)
                ) {
                    Text("Create", fontFamily = MartianMono, color = MaterialTheme.colorScheme.primary)
                }

                if (isJoining) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase().replace("\\s".toRegex(), "") },
                        label = { Text("Enter your code", fontFamily = MartianMono) },
                        textStyle = TextStyle(fontFamily = MartianMono),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                            focusedTextColor = MaterialTheme.colorScheme.secondary,
                            unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                            cursorColor = MaterialTheme.colorScheme.secondary,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier.width(300.dp)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Button(
                        onClick = {
                            if (!joiningInProgress && pseudo.text.isNotBlank() && code.isNotBlank()) {
                                joiningInProgress = true

                                coroutineScope.launch {
                                    firestoreClient.getRoom(code).collect { room ->
                                        if (room != null) {
                                            val player = Player(
                                                pseudo = pseudo.text,
                                                iconUrl = 1,
                                                isHost = false
                                            )
                                            firestoreClient.joinRoomWithAutoId(code, player).collect { playerId ->
                                                joiningInProgress = false
                                                if (playerId != null) {
                                                    IdManager.currentPlayerId = playerId
                                                    IdManager.currentRoomId = code
                                                    onJoinClicked(code, playerId)
                                                } else {
                                                    Log.e("Firebase", "Erreur lors de l'ajout du joueur")
                                                }
                                            }
                                        } else {
                                            joiningInProgress = false
                                            Log.e("Firebase", "Aucune Room trouvée avec le code : $code")
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !joiningInProgress,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        border = BorderStroke(2.dp, Color.White),
                        modifier = Modifier
                            .width(300.dp)
                            .height(56.dp)
                    ) {
                        if (joiningInProgress) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Join",
                                fontFamily = MartianMono,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { isJoining = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        border = BorderStroke(2.dp, Color.White),
                        modifier = Modifier
                            .width(300.dp)
                            .height(56.dp)
                    ) {
                        Text("Join", fontFamily = MartianMono, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun TakeProfilePicture(imageViewModel : ImageViewModel?) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { result: Bitmap? ->
        bitmap = result
    }
    Box(
        //modifier = modifier.size(120.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.beaute),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                //.clip(RoundedCornerShape(25.dp))
                .shadow(8.dp, shape = CircleShape)
                .background(shape = CircleShape, color = Color.White),
        )
        bitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "Captured photo",modifier = Modifier
                .size(120.dp)
                .background(shape = CircleShape, color = Color.White)
                .shadow(8.dp, shape = CircleShape)
                ,
                contentScale = ContentScale.Crop,
            )
            // Save the image to a file
            val context = LocalContext.current
            val internalDir = context.filesDir
            val photoFile = File(internalDir,"temp_photo.jpg")
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(photoFile))
            imageViewModel?.updateImagePath(photoFile.absolutePath)
        }
        IconButton(modifier = Modifier
            .size(36.dp)
            .background(Color.White, shape = CircleShape)
            .align(Alignment.BottomEnd),
            onClick = { takePictureLauncher.launch()
            }) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.Black,
                modifier = Modifier.size(14.dp)
            )
            //Text("Take Photo")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProfilePicture(modifier: Modifier, imageViewModel : ImageViewModel?,isHost: Boolean = false) {
    //var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    Box(
        modifier = modifier
    ) {

        Image(

            painter = painterResource(R.drawable.beaute),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .background(shape = CircleShape, color = Color.White),
        )
        imageViewModel?.imagePath?.let {
                path ->
            val bitmap = BitmapFactory.decodeFile(path)

            // val context = LocalContext.current
            //val internalDir = context.filesDir
            //val photoFile = File(internalDir,imageViewModel?.imagePath?)

            Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Captured photo",modifier = Modifier
                .size(120.dp)
                .background(shape = CircleShape, color = Color.White)
                .clip(CircleShape)
                .shadow(8.dp, shape = CircleShape)
                ,
                contentScale = ContentScale.Crop,
            )


        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AccueilScreen(
        imageViewModel = null,
        onCreateClicked = {},
        onJoinClicked = { _, _ -> }
    )
}
