package com.example.strider.ui.theme.Pages
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val gradientColors = listOf(Color.Cyan, Color.Blue, Color.Magenta)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier:Modifier = Modifier) {
    var presses by remember { mutableIntStateOf(0) }
    var ListeScores by remember { mutableStateOf(listOf(0f)) }

    ListeScores = listOf(15f, 12f, 10f,5f);

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = Color.Green,
                    titleContentColor = Color.White,
                ),
                title = {
                    Text(
                        modifier =  Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        text = "Strider"
                    )
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Green,
                contentColor = Color.White,
            ) {
                Button(modifier = Modifier.fillMaxWidth(),
                    onClick = { }) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = "Pause",
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { presses++ }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier

                .padding(innerPadding),


            verticalAlignment = Alignment.Bottom       ) {
            for (score in ListeScores) {
                PlayerScoreStat(score, 17f)
            }

        }
    }
}
@Composable
fun PlayerScoreStat(distance: Float, distanceMax: Float,modifier: Modifier = Modifier) {

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10))
            .fillMaxHeight(distance/distanceMax)
            .background(
                brush = Brush.linearGradient(colors = gradientColors),
            )
    ) {
        Text(
            text = distance.toString(),
            style = typography.headlineMedium,
            modifier = Modifier.padding(8.dp)
        )

    }

}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen(){
    MainScreen()
}