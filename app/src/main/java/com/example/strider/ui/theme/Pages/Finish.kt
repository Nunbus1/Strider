package com.example.strider.ui.theme.Pages

import DataClass.Player
import ViewModels.ImageViewModel
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.strider.R
import com.example.strider.ui.theme.BricolageGrotesque
import com.example.strider.ui.theme.MartianMono
import com.example.strider.ui.theme.StriderTheme
import kotlinx.coroutines.delay

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

    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color(0xFF252525) else Color.White
    val backgroundRes = if (isDarkTheme) R.drawable.wave_dark else R.drawable.wave
    val textColor = if (isDarkTheme) Color.White else Color.Black

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
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
        Text(
            text = "Statistics",
            fontSize = 60.sp,
            lineHeight = 116.sp,
            style = TextStyle(
                fontFamily = BricolageGrotesque,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(8.dp)
        )

        Text(
            text = "Code : $roomCode",
            fontSize = 20.sp,
            fontFamily = MartianMono,
            color = textColor,
            modifier = Modifier.padding(8.dp)
        )

        ResultSection(
            playerName = sortedPlayers.firstOrNull()?.second?.pseudo ?: "Unknown",
            resultMessage = if (showSpeedState) "The fastest" else "Win this match",
            textColor = textColor
        )


        if (showSpeedState) {
            SpeedGraph(players = players, selectedPlayers = selectedPlayers)
        } else {
            Podium(players = sortedPlayers)
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
            meId = playerId,
            backgroundColor = backgroundColor
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
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .size(32.dp)
                .clickable { if (showSpeedState) {
                    onResetInfoMessage()
                    onBackClicked()
                } else {
                    onHomeClicked()
                }}
        )

        ProfilePicture(
            modifier = Modifier
                .size(50.dp)
                .background(shape = CircleShape, color = Color.White),
            imageViewModel = imageViewModel
        )
    }
}

@Composable
fun ResultSection(playerName: String, resultMessage: String, textColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = playerName,
            fontSize = 20.sp,
            fontFamily = MartianMono,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = resultMessage,
            fontSize = 20.sp,
            fontFamily = MartianMono,
            color = textColor,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun Podium(players: List<Pair<Int, Player>>) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    // Dégradé plus sombre basé sur secondary
    val darkSecondaryShade = Brush.verticalGradient(
        colors = listOf(
            secondary.copy(alpha = 1f),
            secondary.copy(alpha = 0.6f)
        )
    )

    val gradientSecondary = Brush.verticalGradient(
        colors = listOf(
            secondary.copy(alpha = 1f),
            secondary.copy(alpha = 0.85f)
        )
    )

    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.Black else Color.White

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(10.dp)
            .shadow(8.dp, shape = RoundedCornerShape(16.dp))
            .background(
                color = primary, // 👈 fond passe à primary
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

        // MIDDLE
        val middleFaceLeft = Path().apply {
            moveTo(middleX, topY)
            lineTo(middleX - depth, topY - depth)
            lineTo(middleX - depth, topY + podiumHeight - depth)
            lineTo(middleX, topY + podiumHeight)
            close()
        }
        drawPath(middleFaceLeft, darkSecondaryShade)

        val middleFaceRight = Path().apply {
            moveTo(middleX + podiumWidth, topY)
            lineTo(middleX + depth + podiumWidth, topY - depth)
            lineTo(middleX + depth + podiumWidth, topY + podiumHeight - depth)
            lineTo(middleX + podiumWidth, topY + podiumHeight)
            close()
        }
        drawPath(middleFaceRight, darkSecondaryShade)

        val middleTop = Path().apply {
            moveTo(middleX, topY)
            lineTo(middleX + podiumWidth, topY)
            lineTo(middleX + podiumWidth + depth, topY - depth)
            lineTo(middleX - depth, topY - depth)
            close()
        }
        drawPath(middleTop, gradientSecondary)

        drawRect(gradientSecondary, Offset(middleX, topY), Size(podiumWidth, podiumHeight))

        // RIGHT
        val rightFace = Path().apply {
            moveTo(rightX + podiumWidth, topY + rightHeightOffset)
            lineTo(rightX + depth + podiumWidth, topY + rightHeightOffset - depth)
            lineTo(rightX + depth + podiumWidth, topY + podiumHeight - depth)
            lineTo(rightX + podiumWidth, topY + podiumHeight)
            close()
        }
        drawPath(rightFace, darkSecondaryShade)

        val rightTop = Path().apply {
            moveTo(rightX, topY + rightHeightOffset)
            lineTo(rightX + podiumWidth, topY + rightHeightOffset)
            lineTo(rightX + podiumWidth + depth, topY + rightHeightOffset - depth)
            lineTo(rightX + depth, topY + rightHeightOffset - depth)
            close()
        }
        drawPath(rightTop, gradientSecondary)

        drawRect(gradientSecondary, Offset(rightX, topY + rightHeightOffset), Size(podiumWidth, podiumHeight - rightHeightOffset))

        // LEFT
        val leftFace = Path().apply {
            moveTo(leftX, topY + leftHeightOffset)
            lineTo(leftX - depth, topY + leftHeightOffset - depth)
            lineTo(leftX - depth, topY + podiumHeight - depth)
            lineTo(leftX, topY + podiumHeight)
            close()
        }
        drawPath(leftFace, darkSecondaryShade)

        val leftTop = Path().apply {
            moveTo(leftX, topY + leftHeightOffset)
            lineTo(leftX + podiumWidth, topY + leftHeightOffset)
            lineTo(leftX + podiumWidth - depth, topY + leftHeightOffset - depth)
            lineTo(leftX - depth, topY + leftHeightOffset - depth)
            close()
        }
        drawPath(leftTop, gradientSecondary)

        drawRect(gradientSecondary, Offset(leftX, topY + leftHeightOffset), Size(podiumWidth, podiumHeight - leftHeightOffset))

        // CERCLES
        drawCircle(gradientSecondary, radius = iconSize / 2, center = Offset(middleX + podiumWidth / 2 , topY - iconSize ))
        drawCircle(gradientSecondary, radius = iconSize / 2, center = Offset(rightX  + podiumWidth / 2  + depth / 2, topY + rightHeightOffset - iconSize ))
        drawCircle(gradientSecondary, radius = iconSize / 2, center = Offset(leftX + podiumWidth / 2  - depth / 2, topY + leftHeightOffset - iconSize ))

        drawContext.canvas.nativeCanvas.drawText(
            "👑",
            middleX + podiumWidth / 2 ,
            topY - iconSize - 80f,
            android.graphics.Paint().apply {
                textSize = 60f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
        )
        // --- TEXTE : TOP 3 JOUEURS --- //
        val paint = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 40f
            color = textColor.toArgb()
            isAntiAlias = true
            this.typeface = typeface
        }

        val textOffsetY = (paint.descent() + paint.ascent()) / 2

        drawContext.canvas.nativeCanvas.drawText(
            players.getOrNull(0)?.second?.pseudo ?: "1st",
            middleX + podiumWidth / 2,
            topY - iconSize - textOffsetY,
            paint
        )

        drawContext.canvas.nativeCanvas.drawText(
            players.getOrNull(1)?.second?.pseudo ?: "2nd",
            leftX + podiumWidth / 2 - depth / 2,
            topY + leftHeightOffset - iconSize - textOffsetY,
            paint
        )

        drawContext.canvas.nativeCanvas.drawText(
            players.getOrNull(2)?.second?.pseudo ?: "3rd",
            rightX + podiumWidth / 2 + depth / 2,
            topY + rightHeightOffset - iconSize - textOffsetY,
            paint
        )

        // OMBRES
        drawOval(
            darkSecondaryShade,
            topLeft = Offset(middleX + podiumWidth / 2 - iconSize * 0.7f / 2, topY - iconSize / 5),
            size = Size(iconSize * 0.7f, iconSize * 0.1f)
        )
        drawOval(
            darkSecondaryShade,
            topLeft = Offset(rightX  + podiumWidth / 2  + depth / 2 - iconSize * 0.7f / 2, topY + rightHeightOffset - iconSize / 5),
            size = Size(iconSize * 0.7f, iconSize * 0.1f)
        )
        drawOval(
            darkSecondaryShade,
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
    meId: Int,
    backgroundColor: Color
) {
    val gradients = listOf(
        Brush.verticalGradient(colors = listOf(Color(0xFFFFD700), Color(0xFFFFE066))), // Or
        Brush.verticalGradient(colors = listOf(Color(0xFFC0C0C0), Color(0xFFD9D9D9))), // Argent
        Brush.verticalGradient(colors = listOf(Color(0xFFCD7F32), Color(0xFFD8A47F)))  // Bronze
    )

    val meGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        )
    )

    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.Center
    ) {
        players.chunked(3).forEach { group ->
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
                        else -> Brush.verticalGradient(colors = listOf(backgroundColor, backgroundColor))
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
                                if (selectedPlayers.contains(id)) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                CircleShape
                            )
                            .padding(5.dp)
                    ) {
                        Text(
                            text = if (showSpeed) "${player.pseudo} - ${player.distance.value.toInt()} m" else player.pseudo,
                            fontSize = 15.sp,
                            fontFamily = MartianMono,
                            color = textColor,
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
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val lineColor = if (isDark) Color.White else Color.Black
    val pointColor = colorScheme.secondary
    val labelColor = colorScheme.secondary
    val backgroundColor = colorScheme.primary

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
                color = backgroundColor,
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
            drawLine(lineColor, Offset(originX, y), Offset(originX + graphWidth, y), 2f)
            drawContext.canvas.nativeCanvas.drawText(
                "$label", originX - 35f, y + 10f,
                android.graphics.Paint().apply {
                    textSize = 28f
                    color = labelColor.toArgb()
                }
            )
        }

        // Axe X : Distance
        val firstPoints = interpolatedData[selectedPlayers.firstOrNull()] ?: emptyList()
        val totalPoints = firstPoints.size

        if (totalPoints >= 1) {
            val stepCount = 5
            for (i in 0..stepCount) {
                val fraction = i / stepCount.toFloat()
                val distanceKm = fraction * maxDistance
                val x = originX + fraction * graphWidth

                drawLine(lineColor, Offset(x, originY), Offset(x, originY - graphHeight), 2f)
                drawContext.canvas.nativeCanvas.drawText(
                    "%.1f".format(distanceKm), x, originY + 25f,
                    android.graphics.Paint().apply {
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                        color = labelColor.toArgb()
                    }
                )
            }
        }

        // Axes principaux
        drawLine(lineColor, Offset(originX, originY), Offset(originX, originY - graphHeight), strokeWidth = 2f)
        drawLine(lineColor, Offset(originX, originY), Offset(originX + graphWidth, originY), strokeWidth = 2f)

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
            drawPath(path, Brush.linearGradient(listOf(labelColor, lineColor)), style = Stroke(width = 3f))

            points.forEach { (x, y) ->
                val px = originX + (x / maxDistance) * graphWidth
                val py = originY - y * stepY
                drawCircle(pointColor, radius = 4f, center = Offset(px, py))
            }
        }

        // Titres des axes
        drawContext.canvas.nativeCanvas.apply {
            drawText("Distance (km)", originX + graphWidth / 2, originY + 50f,
                android.graphics.Paint().apply {
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.CENTER
                    color = labelColor.toArgb()
                })
            save()
            rotate(90f, originX + graphWidth + 40f, originY - graphHeight / 2)
            drawText("Temps (s)", originX + graphWidth + 40f, originY - graphHeight / 2 + 15,
                android.graphics.Paint().apply {
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.CENTER
                    color = labelColor.toArgb()
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
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color.Black

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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .width(150.dp)
                    .height(56.dp),
            ) {
                Text(
                    text = "Game",
                    fontSize = 20.sp,
                    fontFamily = MartianMono,
                    color = textColor
                )
            }

            Button(
                onClick = { onNextClicked() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier
                    .width(150.dp)
                    .height(56.dp),

            ) {
                Text(
                    text = "Next",
                    fontSize = 20.sp,
                    fontFamily = MartianMono,
                    color = textColor
                )
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