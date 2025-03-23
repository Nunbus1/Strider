package com.example.strider.ui.theme.Pages
import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.strider.R
import com.example.strider.ui.theme.gradientPrimaryColors
import kotlin.math.round

//val gradientColors = listOf(Color.Blue,Color.Cyan )

data class Player(
    val iconUrl: Int,
    val pseudo: String
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(modifier:Modifier = Modifier,
               onPauseClicked: () -> Unit) {
    var presses by remember { mutableIntStateOf(0) }
    var ListeScores by remember { mutableStateOf(listOf(0f)) }

    ListeScores = listOf(15f, 12f, 10f,11f)
    val player1 = Player(1,"test")
    val player2 = Player(1,"test")
    val player3 = Player(1,"test")
    val player4 = Player(1,"test")
var ListePlayer = listOf(player1,player2,player3,player4)

    Column (modifier = modifier
        .fillMaxSize()
        .background(colorScheme.primary)
        .zIndex(-2f)
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween

        ) {
        TopAppBar(modifier=modifier
            .background(brush = Brush.linearGradient(colors = gradientPrimaryColors)),

            title = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {

                Icon(modifier = Modifier.size(50.dp),
                    painter = painterResource(R.drawable.logo), // Use your desired icon
                    contentDescription = "Menu Icon",

                    )
            }

        )


            Row(modifier = modifier//.align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(8.dp)
                .fillMaxHeight(0.7f)
                //.height(200.dp)
                    ,
                verticalAlignment = Alignment.Bottom, // Align children vertically to the center
                horizontalArrangement = Arrangement.SpaceAround,

            ) {
                for (score in ListeScores) {
                    PlayerScoreStat(score, 15f)
                    //Spacer(modifier = Modifier.weight(5f))
                }
            }




        Box(modifier= Modifier.fillMaxWidth()
            .padding(bottom = 16.dp),
            contentAlignment = (Alignment.BottomCenter)
        ) {
            Button(
                onClick = onPauseClicked ,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .shadow(8.dp, shape = RoundedCornerShape(23.dp)),
                colors = ButtonDefaults.buttonColors(
                    Color.Transparent
                ),

                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(23.dp),
            ){Box(modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(gradientPrimaryColors)
                )
                .padding(20.dp)

                ,
                contentAlignment = Alignment.Center
            ){
                Text("Pause")
            }

            }
        }
    }
    //PlayerHorizontalBar(players = ListePlayer, modifier = Modifier)
}

@Composable
fun PlayerScoreStat(distance: Float, distanceMax: Float,modifier: Modifier = Modifier) {
Column (modifier = Modifier//.fillMaxWidth(0.3f)
 ,
    horizontalAlignment = Alignment.CenterHorizontally
        ,

    ) {
    Image(
        painter = painterResource(R.drawable.beaute),
        contentDescription = "Player Icon",
        modifier = Modifier
            .padding(bottom = 10.dp)
            .size(50.dp)
            .clip(CircleShape)
            .background(Color.Cyan)
               )
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10))

            .fillMaxHeight(distance / distanceMax)
            .background(
                brush = Brush.linearGradient(colors = gradientPrimaryColors),
            ),
        //contentAlignment = Alignment.BottomCenter,
    ) {

        Text(
            text = distance.toString(),
            style = typography.bodySmall,
            modifier = Modifier.padding(8.dp)
                .graphicsLayer {
                    rotationZ = 90f
                }
        )

    }
}
}
@Composable
fun PlayerHorizontalBar(players: List<Player>, modifier: Modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top=70.dp, end = 0.dp)
                ,
            contentAlignment = Alignment.TopEnd,
            ) {
            Column(modifier = Modifier
                //.background(color = colorScheme.secondary,
                //    shape = MaterialTheme.shapes.medium)
                .padding(5.dp)
                //.shadow(10.dp,shape = MaterialTheme.shapes.medium))
                    ,
                horizontalAlignment = Alignment.End,
            ) {
                for (player in players) {
                    PlayerIconWithPseudo(player)
                }
            }
        }

}
@Composable
fun PlayerIconWithPseudo(player: Player) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)

    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.beaute),
                contentDescription = "Player Icon",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Cyan)
            )
            //Spacer(modifier = Modifier.height(-15.dp))
            Text(
                text = player.pseudo,
                style = TextStyle(color = colorScheme.onPrimaryContainer, fontSize = 16.sp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewMainScreen(){
    GameScreen( onPauseClicked={})
}
