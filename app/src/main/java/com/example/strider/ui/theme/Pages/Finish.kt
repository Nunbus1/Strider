package com.example.strider.ui.theme.Pages

import DataClass.Player
import ViewModels.ImageViewModel
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlin.math.ceil

@Composable
fun FinishScreen(
    imageViewModel : ImageViewModel?,
    roomCode: String,
    playerId: Int,
    startTime: Long,
    onContinueClicked: (roomCode: String, playerId: Int, startTime: Long) -> Unit,
    onHomeClicked: () -> Unit
) {

    val firestoreClient = remember { FirestoreClient() }
    val players = remember { mutableStateListOf<Pair<Int, Player>>() }

    val elapsed = remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            elapsed.value = (System.currentTimeMillis() - startTime)
            //Log.d("TIMER", "Temps écoulé : ${elapsed.value / 1000}s")
            delay(1000)
        }
    }

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

    var infoMessage by remember { mutableStateOf("Cliquez sur Next pour voir les stats 📊") }

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
            onHomeClicked = onHomeClicked,
            onResetInfoMessage = { infoMessage = "Cliquez sur Next pour voir les stats 📊" },
            imageViewModel = imageViewModel
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
            onNextClicked = {
                showSpeedState = true
                infoMessage = "Sélectionnez un joueur pour voir sa courbe 📈"
            },
            onContinueClicked = onContinueClicked,
            roomCode = roomCode,
            playerId = playerId,
            startTime = startTime,
            infoMessage = infoMessage,
            onResetInfoMessage = { infoMessage = "Cliquez sur Next pour voir les stats 📊" }
        )
    }
}

val gradientBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFF22FFFB), Color(0xFF48AAC5))
)

@Composable
fun HeaderSection(
    showSpeedState: Boolean,
    onBackClicked: () -> Unit,
    onHomeClicked: () -> Unit,
    onResetInfoMessage: () -> Unit,
    imageViewModel : ImageViewModel?
)
{
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
                .clickable { if (showSpeedState) {
                    onResetInfoMessage()
                    onBackClicked()
                } else {
                    onHomeClicked()
                }}
        )

        Text(
            text = stringResource(R.string.app_name),
            fontSize = 50.sp,
            color = Color.Black
        )

        ProfilePicture(modifier = Modifier.size(75.dp)
            .clip(CircleShape), imageViewModel = imageViewModel)
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
    val timeSteps = 15

    val interpolatedData = remember(players, selectedPlayers) {
        players.associate { (id, player) ->
            if (id !in selectedPlayers) return@associate id to emptyList()

            val sorted = player.timedDistance.sortedBy { it.second }
            if (sorted.size < 2) return@associate id to emptyList<Pair<Float, Float>>()

            val startTime = sorted.first().second
            val endTime = sorted.last().second
            val totalDuration = endTime - startTime
            val stepDuration = totalDuration / (timeSteps - 1)

            val points = mutableListOf<Pair<Float, Float>>()
            var currentIndex = 0

            for (i in 0 until timeSteps) {
                val targetTime = startTime + i * stepDuration
                while (currentIndex < sorted.size - 2 && sorted[currentIndex + 1].second < targetTime) {
                    currentIndex++
                }

                val (d1, t1) = sorted[currentIndex]
                val (d2, t2) = sorted[currentIndex + 1]
                val tFraction = (targetTime - t1).toFloat() / (t2 - t1).toFloat()
                val interpolatedDistance = d1 + (d2 - d1) * tFraction
                val relativeTimeSec = (targetTime - startTime) / 1000f

                points.add(interpolatedDistance / 1000f to relativeTimeSec)
            }

            id to points
        }
    }

    val allPoints = selectedPlayers.flatMap { interpolatedData[it] ?: emptyList() }
    val maxTime = allPoints.maxOfOrNull { it.second }?.coerceAtLeast(1f) ?: 1f
    val maxDistance = allPoints.maxOfOrNull { it.first }?.coerceAtLeast(0.1f) ?: 0.1f

    val yLabels = List(5) { i ->
        val raw = i * maxTime / 4f
        ((raw + 9) / 10).toInt() * 10
    }
    val maxYLabel = yLabels.last()

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
        val graphWidth = size.width - 60f
        val graphHeight = size.height - 60f
        val originX = 40f
        val originY = size.height - 30f

        val stepX = graphWidth / (timeSteps - 1)
        val stepY = graphHeight / maxYLabel

        // Axe Y : Temps (s)
        yLabels.forEach { label ->
            val y = originY - (label / maxYLabel.toFloat()) * graphHeight
            drawLine(Color.White, Offset(originX, y), Offset(originX + graphWidth, y), 2f)
            drawContext.canvas.nativeCanvas.drawText(
                "$label", originX - 35f, y + 10f,
                android.graphics.Paint().apply {
                    textSize = 28f
                    color = android.graphics.Color.BLACK
                }
            )
        }

        // Axe X : 15 distances exactes du premier joueur sélectionné
        val firstPoints = interpolatedData[selectedPlayers.firstOrNull()] ?: emptyList()
        val totalPoints = firstPoints.size

        if (totalPoints >= 1) {
            val stepCount = 5 // Nombre souhaité de graduations
            for (i in 0..stepCount) {
                val fraction = i / stepCount.toFloat()
                val distanceKm = fraction * maxDistance
                val x = originX + fraction * graphWidth

                drawLine(Color.White, Offset(x, originY), Offset(x, originY - graphHeight), 2f)
                drawContext.canvas.nativeCanvas.drawText(
                    "%.1f".format(distanceKm), x - 15f, originY + 25f,
                    android.graphics.Paint().apply {
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                        color = android.graphics.Color.BLACK
                    }
                )
            }
        }


        // Axes principaux
        drawLine(Color.Black, Offset(originX, originY), Offset(originX, originY - graphHeight), strokeWidth = 2f)
        drawLine(Color.Black, Offset(originX, originY), Offset(originX + graphWidth, originY), strokeWidth = 2f)

        // Tracés
        selectedPlayers.forEach { id ->
            val points = interpolatedData[id] ?: return@forEach
            if (points.size < 2) return@forEach

            val path = Path().apply {
                val (x0, y0) = points.first()
                moveTo(originX + (x0 / maxDistance) * graphWidth, originY - y0 * stepY)
                points.drop(1).forEach { (x, y) ->
                    val px = originX + (x / maxDistance) * graphWidth
                    val py = originY - y * stepY
                    lineTo(px, py)
                }
            }
            drawPath(path, gradientBrush, style = Stroke(width = 3f))

            points.forEach { (x, y) ->
                val px = originX + (x / maxDistance) * graphWidth
                val py = originY - y * stepY
                drawCircle(Color.Black, radius = 4f, center = Offset(px, py))
            }
        }

        // Titres des axes
        drawContext.canvas.nativeCanvas.apply {
            drawText("Distance (km)", originX + graphWidth / 2, originY + 50f,
                android.graphics.Paint().apply {
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.CENTER
                    color = android.graphics.Color.BLACK
                })
            save()
            rotate(90f, originX + graphWidth + 40f, originY - graphHeight / 2)
            drawText("Temps (s)", originX + graphWidth + 40f, originY - graphHeight / 2 + 15,
                android.graphics.Paint().apply {
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.CENTER
                    color = android.graphics.Color.BLACK
                })
            restore()
        }
    }
}





fun getInterpolatedDistanceAndTimePoints(
    timedDistance: List<Pair<Float, Long>>,
    steps: Int = 15
): List<Pair<Float, Float>> {
    if (timedDistance.size < 2) return List(steps) { 0f to 0f }

    val sorted = timedDistance.sortedBy { it.second }
    val startTime = sorted.first().second
    val endTime = sorted.last().second
    val totalDuration = endTime - startTime
    val stepDuration = totalDuration / (steps - 1)

    val result = mutableListOf<Pair<Float, Float>>()
    var currentIndex = 0

    for (i in 0 until steps) {
        val targetTime = startTime + i * stepDuration

        while (currentIndex < sorted.size - 2 && sorted[currentIndex + 1].second < targetTime) {
            currentIndex++
        }

        val (d1, t1) = sorted[currentIndex]
        val (d2, t2) = sorted[currentIndex + 1]

        val tFraction = (targetTime - t1).toFloat() / (t2 - t1).toFloat()
        val interpolatedDistance = d1 + (d2 - d1) * tFraction
        val relativeTimeSec = (targetTime - startTime).toFloat() / 1000f

        result.add(interpolatedDistance / 1000f to relativeTimeSec)
    }

    return result
}



fun getInterpolatedSpeedPoints(
    timedDistance: List<Pair<Float, Long>>,
    steps: Int = 15,
    inKmPerHour: Boolean = true // sinon c’est en km/min
): List<Float> {
    if (timedDistance.size < 2) return List(steps) { 0f }

    val sorted = timedDistance.sortedBy { it.second }
    val startTime = sorted.first().second
    val endTime = sorted.last().second
    val totalDuration = endTime - startTime

    val stepDuration = totalDuration / (steps - 1)

    val result = mutableListOf<Float>()
    var currentIndex = 0

    for (i in 0 until steps) {
        val targetTime = startTime + i * stepDuration

        // Avancer dans la liste pour trouver les 2 points autour du temps voulu
        while (currentIndex < sorted.size - 2 && sorted[currentIndex + 1].second < targetTime) {
            currentIndex++
        }

        val (d1, t1) = sorted[currentIndex]
        val (d2, t2) = sorted[currentIndex + 1]

        val deltaDistance = d2 - d1
        val deltaTime = (t2 - t1).toFloat() / 60000f // en minutes
        val baseSpeed = if (deltaTime > 0f) deltaDistance / deltaTime else 0f

        // Optionnel : convertir en km/h
        val speed = if (inKmPerHour) baseSpeed * 60f else baseSpeed

        result.add(speed)
    }

    return result
}

@Composable
fun ActionButtons(
    onNextClicked: () -> Unit,
    onContinueClicked: (String, Int, Long) -> Unit,
    roomCode: String,
    playerId: Int,
    startTime: Long,
    infoMessage: String,
    onResetInfoMessage: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = infoMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { onContinueClicked(roomCode, playerId, startTime) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(colors = listOf(Color(0xFF22A6FF), Color(0xFF0044FF))),
                        shape = CircleShape
                    )
                    .width(150.dp)
            ) {
                Text(text = "Game", fontSize = 20.sp, color = Color.White)
            }

            Button(
                onClick = {
                    onNextClicked()
                },
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
}

@Preview(showBackground = true)
@Composable
fun FinishScreenPreview() {
    StriderTheme {
        FinishScreen(null,roomCode = "",
            playerId = 0, startTime = System.currentTimeMillis(),{ _, _, _ -> }, {})
    }
}