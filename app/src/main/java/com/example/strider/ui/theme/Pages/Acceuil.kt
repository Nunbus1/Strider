package com.example.strider.ui.theme.Pages


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.strider.R

@Composable
fun AccueilScreen(
    onCreateClicked: () -> Unit,
    onJoinClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var pseudo by remember { mutableStateOf("Pseudo") }
    var isJoining by remember { mutableStateOf(false) }

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
        Spacer(modifier = Modifier.height(16.dp))
        ProfilePicture()
        TextField(
            value = pseudo,
            onValueChange = { pseudo = it },
            textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onCreateClicked,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(colors = listOf(Color(0xFF22A6FF), Color(0xFF0044FF))),
                        shape = CircleShape
                    )
                    .width(150.dp)
            ) {
                Text("Create")

            }
            if (isJoining) {
                // Zone de texte + bouton "Join"
                OutlinedTextField(
                    value = pseudo,
                    onValueChange = { pseudo = it },
                    label = { Text("Enter your pseudo") },
                    modifier = Modifier.width(200.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (pseudo.isNotBlank()) {
                            onJoinClicked(pseudo)
                        }
                    },
                    modifier = Modifier.width(150.dp)
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
fun ProfilePicture(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(120.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.beaute),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(25.dp))
        )

        // Icône "+" en bas à droite
        Box(
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.BottomEnd)
                .background(Color.White, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AccueilScreen(
        onCreateClicked = {},
        onJoinClicked = {}
    )
}