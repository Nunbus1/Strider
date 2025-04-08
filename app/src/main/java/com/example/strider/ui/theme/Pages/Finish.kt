package com.example.strider.ui.theme.Pages

import DataClass.Player
import ViewModels.ImageViewModel
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.strider.R
import com.example.strider.ui.theme.StriderTheme
import kotlin.random.Random
import com.example.strider.ui.theme.StriderTheme
import com.example.strider.ui.theme.gradientPrimaryColors
import com.example.strider.ui.theme.gradientSecondaryColor
import kotlinx.coroutines.flow.Flow

@Composable
fun FinishScreen(
    imageViewModel : ImageViewModel?,
    roomCode: String,
    playerId: Int,
    onContinueClicked: (roomCode: String, playerId: Int) -> Unit,
    onHomeClicked: () -> Unit
) {

    val firestoreClient = remember { FirestoreClient() }
    val players = remember { mutableStateListOf<Pair<Int, Player>>() }

    LaunchedEffect(roomCode) {
        firestoreClient.getPlayersInRoom(roomCode).collect { newPlayers ->
            players.clear()
            players.addAll(newPlayers)

            newPlayers.forEach { (id, player) ->
                Log.d("Debug", "Player[$id] = ${player.pseudo}")
            }
        }

    }
    val sortedPlayers by remember {
        derivedStateOf {
            players.sortedByDescending { it.second.distance.value }
        }
    }

    var showSpeedState by remember { mutableStateOf(false) }
    //val meIndex = 4
    var selectedPlayers by remember { mutableStateOf(setOf(playerId)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        HeaderSection(
            showSpeedState = showSpeedState,
            onBackClicked = { showSpeedState = false },
            onHomeClicked = onHomeClicked // Passe la navigation vers Acceuil
        )
        Spacer(modifier = Modifier.height(10.dp))
        ResultSection(
            playerName = sortedPlayers.firstOrNull()?.second?.pseudo ?: "Unknown",
            resultMessage = if (showSpeedState) "The fastest" else "Win this match")
        Spacer(modifier = Modifier.height(10.dp))
        if (showSpeedState) {
            SpeedGraph(players = players, selectedPlayers = selectedPlayers)
        } else {
            Podium()
        }
        Spacer(modifier = Modifier.height(10.dp))
        PlayerRanking(
            players = sortedPlayers,
            showSpeed = showSpeedState,
            selectedPlayers = selectedPlayers,
            onPlayerClick = { clickedId ->
                selectedPlayers = if (selectedPlayers.contains(clickedId))
                    selectedPlayers - clickedId
                else
                    selectedPlayers + clickedId
            },
            meId = playerId
        )
        Spacer(modifier = Modifier.height(10.dp))
        ActionButtons(
            onNextClicked = { showSpeedState = true },
            onContinueClicked = onContinueClicked,
            roomCode = roomCode,
            playerId = playerId
        )
    }
}

val gradientBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFF22FFFB), Color(0xFF48AAC5))
)

@Composable
fun HeaderSection(showSpeedState: Boolean, onBackClicked: () -> Unit, onHomeClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = if (showSpeedState)
                androidx.compose.material.icons.Icons.Default.ArrowBack
            else
                androidx.compose.material.icons.Icons.Default.Home, // Home quand showSpeedState est false
            contentDescription = if (showSpeedState) "Back" else "Home",
            modifier = Modifier
                .size(32.dp)
                .clickable { if (showSpeedState) onBackClicked() else onHomeClicked() } // Navigation dynamique
        )

        Text(
            text = stringResource(R.string.app_name),
            fontSize = 50.sp,
            color = Color.Black
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Gray, CircleShape)
        )
    }
}

@Composable
fun ResultSection(playerName: String, resultMessage: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = playerName,
            fontSize = 32.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = resultMessage,
            fontSize = 32.sp,
            color = Color.Black
        )
    }
}

@Composable
fun Podium() {
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(250.dp)
        .padding(10.dp)
        .shadow(8.dp, shape = RoundedCornerShape(16.dp)) // Ajout de l'ombre pour un effet de relief
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFE8E8E8), Color(0xFFD0D0D0))
            ),
            shape = RoundedCornerShape(16.dp)
        )
        .padding(10.dp),
    ) {
        val centerX = size.width / 2
        val podiumWidth = 220f
        val podiumHeight = 340f
        val leftX = centerX - (1.5f * podiumWidth)
        val middleX = centerX - (0.5f * podiumWidth)
        val rightX = centerX + (0.5f * podiumWidth)
        val topY = 350f
        val depth = 50f
        val leftHeightOffset = 200f
        val rightHeightOffset = 100f
        val iconSize = 200f

        val darkShade = Brush.verticalGradient(colors = listOf(Color(0xFF22DCD9), Color(0xFF4398AF)))
        //-------- Middle --------//
        val middleFaceLeft = Path().apply {
            moveTo(middleX, topY)
            lineTo(middleX - depth, topY - depth)
            lineTo(middleX - depth, topY + podiumHeight - depth)
            lineTo(middleX, topY + podiumHeight)
            close()
        }
        drawPath(middleFaceLeft, darkShade)
        //drawPath(middleFaceLeft, Color.Black, style = Stroke(width = 4f))

        val middleFaceRight = Path().apply {
            moveTo(middleX + podiumWidth, topY)
            lineTo(middleX + depth + podiumWidth, topY - depth)
            lineTo(middleX + depth + podiumWidth, topY + podiumHeight - depth)
            lineTo(middleX + podiumWidth, topY + podiumHeight)
            close()
        }
        drawPath(middleFaceRight, darkShade)
        //drawPath(middleFaceRight, Color.Black, style = Stroke(width = 4f))

        val middleTop = Path().apply {
            moveTo(middleX, topY)
            lineTo(middleX + podiumWidth, topY)
            lineTo(middleX + podiumWidth + depth, topY - depth)
            lineTo(middleX - depth, topY - depth)
            close()
        }
        drawPath(middleTop, gradientBrush)
        //drawPath(middleTop, Color.Black, style = Stroke(width = 4f))

        drawRect(gradientBrush, topLeft = Offset(middleX, topY), size = androidx.compose.ui.geometry.Size(podiumWidth, podiumHeight))
        //drawRect(Color.Black, topLeft = Offset(middleX, topY), size = androidx.compose.ui.geometry.Size(podiumWidth, podiumHeight), style = Stroke(width = 4f))

        //-------- Right --------//
        val rightFace = Path().apply {
            moveTo(rightX + podiumWidth, topY + rightHeightOffset)
            lineTo(rightX + depth + podiumWidth, topY + rightHeightOffset - depth)
            lineTo(rightX + depth + podiumWidth, topY + podiumHeight - depth)
            lineTo(rightX + podiumWidth, topY + podiumHeight)
            close()
        }
        drawPath(rightFace, darkShade)
        //drawPath(rightFace, Color.Black, style = Stroke(width = 4f))

        val rightTop = Path().apply {
            moveTo(rightX, topY + rightHeightOffset)
            lineTo(rightX + podiumWidth, topY + rightHeightOffset)
            lineTo(rightX + podiumWidth + depth, topY + rightHeightOffset - depth)
            lineTo(rightX + depth, topY + rightHeightOffset - depth)
            close()
        }
        drawPath(rightTop, gradientBrush)
        //drawPath(rightTop, Color.Black, style = Stroke(width = 4f))

        drawRect(gradientBrush, topLeft = Offset(rightX, topY + rightHeightOffset), size = androidx.compose.ui.geometry.Size(podiumWidth, podiumHeight - rightHeightOffset))
        //drawRect(Color.Black, topLeft = Offset(rightX, topY + rightHeightOffset), size = androidx.compose.ui.geometry.Size(podiumWidth, podiumHeight - rightHeightOffset), style = Stroke(width = 4f))

        //-------- Left --------//
        val leftFace = Path().apply {
            moveTo(leftX, topY + leftHeightOffset)
            lineTo(leftX - depth, topY + leftHeightOffset - depth)
            lineTo(leftX - depth, topY + podiumHeight - depth)
            lineTo(leftX, topY + podiumHeight)
            close()
        }
        drawPath(leftFace, darkShade)
        //drawPath(leftFace, Color.Black, style = Stroke(width = 4f))

        val leftTop = Path().apply {
            moveTo(leftX, topY + leftHeightOffset)
            lineTo(leftX + podiumWidth, topY + leftHeightOffset)
            lineTo(leftX + podiumWidth - depth, topY + leftHeightOffset - depth)
            lineTo(leftX - depth, topY + leftHeightOffset - depth)
            close()
        }
        drawPath(leftTop, gradientBrush)
        //drawPath(leftTop, Color.Black, style = Stroke(width = 4f))

        drawRect(gradientBrush, topLeft = Offset(leftX, topY + leftHeightOffset), size = androidx.compose.ui.geometry.Size(podiumWidth, podiumHeight - leftHeightOffset))
        //drawRect(Color.Black, topLeft = Offset(leftX, topY + leftHeightOffset), size = androidx.compose.ui.geometry.Size(podiumWidth, podiumHeight - leftHeightOffset), style = Stroke(width = 4f))

        //-------- Icone --------//
        drawCircle(gradientBrush, radius = iconSize / 2, center = Offset(middleX + podiumWidth / 2 , topY - iconSize ))
        drawCircle(gradientBrush, radius = iconSize / 2, center = Offset(rightX  + podiumWidth / 2  + depth / 2, topY + rightHeightOffset - iconSize ))
        drawCircle(gradientBrush, radius = iconSize / 2, center = Offset(leftX + podiumWidth / 2  - depth / 2, topY + leftHeightOffset - iconSize ))

        //-------- Ombre sous icônes --------//
        drawOval(
            darkShade,
            topLeft = Offset(middleX + podiumWidth / 2 - iconSize * 0.7f / 2, topY - iconSize / 5),
            size = Size(iconSize * 0.7f, iconSize * 0.1f)
        )
        drawOval(
            darkShade,
            topLeft = Offset(rightX  + podiumWidth / 2  + depth / 2 - iconSize * 0.7f / 2, topY + rightHeightOffset - iconSize / 5),
            size = Size(iconSize * 0.7f, iconSize * 0.1f)
        )
        drawOval(
            darkShade,
            topLeft = Offset(leftX + podiumWidth / 2  - depth / 2 - iconSize * 0.7f / 2, topY + leftHeightOffset - iconSize / 5),
            size = Size(iconSize * 0.7f, iconSize * 0.1f)
        )
    }
}

@Composable
fun PlayerRanking(
    players: List<Pair<Int, Player>>,
    showSpeed: Boolean,
    selectedPlayers: Set<Int>,
    onPlayerClick: (Int) -> Unit,
    meId: Int
) {
    val gradients = listOf(
        Brush.verticalGradient(colors = listOf(Color(0xFFFFD700), Color(0xFFFFE066))), // Or
        Brush.verticalGradient(colors = listOf(Color(0xFFC0C0C0), Color(0xFFD9D9D9))), // Argent
        Brush.verticalGradient(colors = listOf(Color(0xFFCD7F32), Color(0xFFD8A47F)))  // Bronze
    )

    val meGradient = Brush.verticalGradient(colors = listOf(Color(0xFF22FFFB), Color(0xFF48AAC5)))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.Center
    ) {
        players.chunked(5).forEach { group ->
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .padding(horizontal = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                group.forEachIndexed { index, (id, player) ->
                    val isMe = id == meId
                    val rank = players.indexOfFirst { it.first == id }
                    val backgroundBrush = when {
                        isMe -> meGradient
                        rank == 0 -> gradients[0]
                        rank == 1 -> gradients[1]
                        rank == 2 -> gradients[2]
                        else -> Brush.verticalGradient(colors = listOf(Color.White, Color.White))
                    }

                    Box(
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxWidth()
                            .shadow(8.dp, shape = RoundedCornerShape(16.dp))
                            .clickable { onPlayerClick(id) }
                            .background(backgroundBrush, shape = CircleShape)
                            .border(
                                2.dp,
                                if (selectedPlayers.contains(id)) Color.Black else Color.Transparent,
                                CircleShape
                            )
                            .padding(5.dp)
                    ) {
                        Text(
                            text = if (showSpeed) "${player.pseudo} - ${player.distance.value.toInt()}km/h" else player.pseudo,
                            fontSize = 15.sp,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SpeedGraph(players: List<Pair<Int, Player>>, selectedPlayers: Set<Int>) {
    val timeSteps = 16
    val speedData = remember {
        players.associate { (id, _) ->
            id to List(timeSteps) { Random.nextInt(1, 25).toFloat() }
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(10.dp)
            .shadow(8.dp, shape = RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE8E8E8), Color(0xFFD0D0D0))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(10.dp)
    ) {
        val graphWidth = size.width - 50f
        val graphHeight = size.height - 50f
        val stepX = graphWidth / (timeSteps - 1)
        val stepY = graphHeight / 15f
        val originX = 20f
        val originY = size.height - 20f

        // Axes labels
        for (i in 0 until timeSteps) {
            drawContext.canvas.nativeCanvas.drawText(
                "$i", originX + i * stepX - 10, originY + 22f,
                android.graphics.Paint().apply {
                    textSize = 30f
                    color = android.graphics.Color.BLACK
                }
            )
        }
        for (i in 0..30 step 5) {
            drawContext.canvas.nativeCanvas.drawText(
                "$i", originX - 40f, originY - i * stepY / 2 + 10,
                android.graphics.Paint().apply {
                    textSize = 30f
                    color = android.graphics.Color.BLACK
                }
            )
        }

        // Axes lines
        for (i in 0..timeSteps) {
            val x = originX + i * stepX
            drawLine(Color.White, Offset(x, originY), Offset(x, originY - graphHeight), strokeWidth = 2f)
        }
        for (i in 0..30 step 5) {
            val y = originY - i * stepY / 2
            drawLine(Color.White, Offset(originX, y), Offset(originX + graphWidth, y), strokeWidth = 2f)
        }

        drawLine(Color.Black, Offset(originX, originY), Offset(originX, originY - graphHeight), strokeWidth = 3f)
        drawLine(Color.Black, Offset(originX, originY), Offset(originX + graphWidth, originY), strokeWidth = 3f)

        // Labels
        drawContext.canvas.nativeCanvas.apply {
            drawText(
                "Time", originX + graphWidth / 2, originY - graphHeight - 20f,
                android.graphics.Paint().apply {
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.CENTER
                    color = android.graphics.Color.BLACK
                }
            )
            save()
            rotate(90f, originX + graphWidth + 40f, originY - graphHeight / 2)
            drawText(
                "Speed", originX + graphWidth + 40f, originY - graphHeight / 2 + 15,
                android.graphics.Paint().apply {
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.CENTER
                    color = android.graphics.Color.BLACK
                }
            )
            restore()
        }

        // Tracer les vitesses
        selectedPlayers.forEach { playerId ->
            val speeds = speedData[playerId]
            if (speeds != null) {
                val path = Path().apply {
                    moveTo(originX, originY - speeds[0] * stepY / 2)
                    for (i in 1 until timeSteps) {
                        lineTo(originX + i * stepX, originY - speeds[i] * stepY / 2)
                    }
                }
                drawPath(path, gradientBrush, style = Stroke(width = 3f))
                speeds.forEachIndexed { i, speed ->
                    drawCircle(Color.Black, 4f, Offset(originX + i * stepX, originY - speed * stepY / 2))
                }
            }
        }
    }
}




@Composable
fun ActionButtons(
    onNextClicked: () -> Unit,
    onContinueClicked: (String, Int) -> Unit,
    roomCode: String,
    playerId: Int
)
{
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick =  {onContinueClicked(roomCode, playerId) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(colors = listOf(Color(0xFF22A6FF), Color(0xFF0044FF))),
                    shape = CircleShape
                )
                .width(150.dp)
        ) {
            Text(text = "Continue", fontSize = 20.sp, color = Color.White)
        }

        Button(
            onClick = onNextClicked,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(colors = listOf(Color(0xFFFF4444), Color(0xFFFF2266))),
                    shape = CircleShape
                )
                .width(150.dp)
        ) {
            Text(text = "Next", fontSize = 20.sp, color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FinishScreenPreview() {
    StriderTheme {
        FinishScreen(null,roomCode = "",
            playerId = 0, { _, _ -> }, {})
    }
}