package com.example.strider.ui.theme.Pages


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.strider.R

@Composable
fun HomeScreen(
    onCreateClicked: () -> Unit,
    onJoinClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pseudo by remember { mutableStateOf("Pseudo") }

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
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(25.dp))
        )
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
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Create")

            }
            Button(
                onClick = onJoinClicked,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Join")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onCreateClicked = {},
        onJoinClicked = {}
    )
}