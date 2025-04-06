package com.example.strider.ui.theme.Pages


import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.strider.R
import ViewModels.ImageViewModel
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths
import android.content.Context
import android.graphics.BitmapFactory
import android.location.Location
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import com.google.android.gms.location.LocationResult


@Composable
fun AccueilScreen(imageViewModel : ImageViewModel? ,
                  player : DataClass.Player,
                  onCreateClicked: () -> Unit,
                  onJoinClicked: (String) -> Unit,
                  modifier: Modifier = Modifier
) {
    player.isHost=false


    var pseudo by remember { mutableStateOf("Pseudo") }
    var code by remember { mutableStateOf("")}
    var isJoining by remember { mutableStateOf(false) }

    // Gérer le bouton retour du téléphone
    BackHandler(isJoining) {
        isJoining = false
    }
    //val image = painterResource(R.drawable.fond)

    /*Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = image,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.5f), // Ajuste l'opacité (0.0 = totalement transparent, 1.0 = opaque)
            contentScale = ContentScale.Crop // Permet d'éviter la déformation
        )
    }*/

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier
                .padding(8.dp),
            text = stringResource(R.string.app_name),
            fontSize = 100.sp,
            lineHeight = 116.sp,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(R.drawable.logo), // Remplace avec ton logo
            contentDescription = "Logo",
            modifier = Modifier.size(150.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        TakeProfilePicture( imageViewModel)
        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            value = pseudo,
            onValueChange = { pseudo = it },
            textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isJoining) {
                
                Button(
                    onClick = onCreateClicked,
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
                    Text("Create")

                }
            }
            if (isJoining) {
                // Zone de texte + bouton "Join"
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Enter your code") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),

                    modifier = Modifier.width(200.dp)
                )

                Button(
                    onClick = {
                        if (pseudo.isNotBlank()) {
                            player.pseudo = pseudo
                            onJoinClicked(pseudo)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(colors = listOf(Color(0xFFFF4444), Color(0xFFFF2266))),
                            shape = CircleShape
                        )
                        .width(150.dp)
                ) {
                    Text("Join")
                }
            } else {
                // Bouton initial "Join"
                Button(
                    onClick = { isJoining = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(colors = listOf(Color(0xFFFF4444), Color(0xFFFF2266))),
                            shape = CircleShape
                        )
                        .width(150.dp)
                ) {
                    Text("Join")
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
        if(isHost){
            Image(

                painter = painterResource(R.drawable.crown),
                contentDescription = "Profile Picture Crown",
                modifier = Modifier.fillMaxSize(0.2f).background(Color.Transparent)
            )
        }


    }
}



@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
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

    AccueilScreen(imageViewModel = null,
        player = testplayer,
        onCreateClicked = {},
        onJoinClicked = {}
    )
}