package com.example.strider.ui.theme.Pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.strider.R

@Composable
fun CreateScreen(
    onBackClicked: () -> Unit,
    onStartClicked: () -> Unit,
    modifier: Modifier = Modifier
) {var description by remember { mutableStateOf("dada") }

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
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Strider",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Encadré "menu"
        Card(
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(2.dp, Color.Blue),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(
                text = "menu",
                fontSize = 20.sp,
                color = Color.Blue,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Séparation avec "Runners"
        Divider(color = Color.Gray, thickness = 1.dp)
        Text(
            text = "settings",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Divider(color = Color.Gray, thickness = 1.dp)

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "GameMode",
            fontSize = 20.sp,
            modifier = Modifier.padding(8.dp)
        )
        Row (
            verticalAlignment = Alignment.CenterVertically
        ){
            Button(
                onClick = onStartClicked,
                modifier = Modifier
                    ,
                colors = ButtonDefaults.buttonColors(
                    Color.Transparent
                ),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(23.dp),
            ){Box(modifier = Modifier
                .background(
                    brush = Brush.linearGradient(gradientColors)).
                padding(10.dp),
                contentAlignment = Alignment.Center
            ){
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
                onClick = onStartClicked,
                modifier = Modifier
                    ,
                colors = ButtonDefaults.buttonColors(
                    Color.Transparent
                ),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(23.dp),
            ){Box(modifier = Modifier
                .background(
                    brush = Brush.linearGradient(gradientColors)).
                padding(10.dp),
                contentAlignment = Alignment.Center
            ){
                Text(">")
            }

            }
        }

        Text(
            text = "description",
            fontSize = 20.sp,
            modifier = Modifier.padding(8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Enter your text:",

        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = description,
            onValueChange = { newText -> description = newText },
            label = { Text("Your input") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
                    // Bouton Start
        Box(
            modifier = Modifier.fillMaxSize()
            ,

        ) {
            Button(
                onClick = onStartClicked,
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
            ){Box(modifier = Modifier.fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(gradientColors))
                .padding(20.dp)

                ,
                contentAlignment = Alignment.Center
            ){
                Text("Create")
            }

            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun CreateScreenPreview() {
    CreateScreen(
        onBackClicked = {},
        onStartClicked = {}
    )
}
