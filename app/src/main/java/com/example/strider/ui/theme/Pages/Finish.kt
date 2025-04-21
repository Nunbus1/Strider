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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.strider.ui.theme.BricolageGrotesque
import com.example.strider.ui.theme.MartianMono
import com.example.strider.ui.theme.StriderTheme
import kotlinx.coroutines.delay

/**
 * Composable qui affiche l'écran de fin de partie.
 * Affiche un podium des joueurs, des statistiques de course (temps, courbes de vitesse),
 * et permet de naviguer vers l'accueil ou de rejouer avec les mêmes données.
 *
 * @param imageViewModel ViewModel contenant la photo du joueur local.
 * @param roomCode Code de la room Firebase.
 * @param playerId Identifiant du joueur courant.
 * @param startTime Heure de début de la partie (timestamp).
 * @param onContinueClicked Fonction appelée pour revenir en jeu ou rejouer.
 * @param onHomeClicked Fonction appelée pour revenir à l'écran d'accueil.
 */
@Composable
fun FinishScreen(
    imageViewModel: ImageViewModel?,
    roomCode: String,
    playerId: Int,
    startTime: Long,
    onContinueClicked: (roomCode: String, playerId: Int, startTime: Long) -> Unit,
    onHomeClicked: () -> Unit
) {
    // -------------------------
    // État local
    // -------------------------

    var hasClickedNext by remember { mutableStateOf(false) }

    // -------------------------
    // Firestore et joueurs
    // -------------------------

    val firestoreClient = remember { FirestoreClient() }
    val players = remember { mutableStateListOf<Pair<Int, Player>>() }

    // -------------------------
    // Temps écoulé
    // -------------------------

    val elapsed = remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            elapsed.longValue = System.currentTimeMillis() - startTime
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

    // -------------------------
    // Classement et sélection
    // -------------------------

    val sortedPlayers by remember {
        derivedStateOf {
            players.sortedByDescending { it.second.distance.floatValue }
        }
    }

    var selectedPlayers by remember { mutableStateOf(setOf(playerId)) }
    var showSpeedState by remember { mutableStateOf(false) }
    var infoMessage by remember { mutableStateOf("Cliquez sur Next pour voir les stats 📊") }

    // -------------------------
    // Thème et apparence
    // -------------------------

    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color(0xFF252525) else Color.White
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
            imageViewModel = imageViewModel,
            onResetNextState = { hasClickedNext = false },
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
                hasClickedNext = true
            },
            onContinueClicked = onContinueClicked,
            roomCode = roomCode,
            playerId = playerId,
            startTime = startTime,
            infoMessage = infoMessage,
            hasClickedNext = hasClickedNext,
            onResetClickedNext = {
                onHomeClicked()
            }
        )
    }
}

/**
 * Composable qui affiche l'en-tête en haut de l'écran des statistiques.
 * Affiche soit un bouton retour (si on visualise les courbes), soit un bouton accueil.
 * Affiche également la photo de profil du joueur courant.
 *
 * @param showSpeedState État indiquant si les courbes de vitesse sont affichées.
 * @param onBackClicked Fonction appelée lorsque l'utilisateur clique sur le bouton retour.
 * @param onHomeClicked Fonction appelée lorsque l'utilisateur clique sur le bouton accueil.
 * @param onResetInfoMessage Fonction utilisée pour réinitialiser le message d'information.
 * @param onResetNextState Fonction utilisée pour réinitialiser le texte du bouton "Next".
 * @param imageViewModel ViewModel contenant la photo du joueur courant.
 */
@Composable
fun HeaderSection(
    showSpeedState: Boolean,
    onBackClicked: () -> Unit,
    onHomeClicked: () -> Unit,
    onResetInfoMessage: () -> Unit,
    onResetNextState: () -> Unit,
    imageViewModel: ImageViewModel?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = if (showSpeedState)
                Icons.AutoMirrored.Filled.ArrowBack
            else
                Icons.Default.Home,
            contentDescription = if (showSpeedState) "Back" else "Home",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .size(32.dp)
                .clickable {
                    if (showSpeedState) {
                        onResetInfoMessage()
                        onResetNextState()
                        onBackClicked()
                    } else {
                        onHomeClicked()
                    }
                }
        )

        ProfilePicture(
            modifier = Modifier
                .size(50.dp)
                .background(shape = CircleShape, color = Color.White),
            imageViewModel = imageViewModel
        )
    }
}



/**
 * Composable qui affiche le nom du joueur gagnant ou le joueur le plus rapide,
 * ainsi qu’un message de résultat lié à sa performance.
 *
 * @param playerName Nom du joueur affiché en haut.
 * @param resultMessage Message associé au résultat (ex : "Win this match" ou "The fastest").
 * @param textColor Couleur du texte (adaptée au thème).
 */
@Composable
fun ResultSection(
    playerName: String,
    resultMessage: String,
    textColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
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


/**
 * Composable qui dessine un podium en 3D avec les trois meilleurs joueurs.
 * Affiche les plateformes gauche (2e), centre (1er), droite (3e),
 * avec leur pseudo respectif et une icône 👑 pour le joueur en 1re position.
 *
 * @param players Liste des joueurs triés par score (Pair<id, Player>), les 3 premiers sont affichés.
 */
@Composable
fun Podium(players: List<Pair<Int, Player>>) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    // -------------------------
    // Dégradés & thème
    // -------------------------

    val darkSecondaryShade = Brush.verticalGradient(
        colors = listOf(secondary.copy(alpha = 1f), secondary.copy(alpha = 0.6f))
    )

    val gradientSecondary = Brush.verticalGradient(
        colors = listOf(secondary.copy(alpha = 1f), secondary.copy(alpha = 0.85f))
    )

    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.Black else Color.White


    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(10.dp)
            .shadow(8.dp, shape = RoundedCornerShape(16.dp))
            .background(color = primary, shape = RoundedCornerShape(16.dp))
            .padding(10.dp)
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

        // 1er - Centre
        drawPath(Path().apply {
            moveTo(middleX, topY)
            lineTo(middleX - depth, topY - depth)
            lineTo(middleX - depth, topY + podiumHeight - depth)
            lineTo(middleX, topY + podiumHeight)
            close()
        }, darkSecondaryShade)

        drawPath(Path().apply {
            moveTo(middleX + podiumWidth, topY)
            lineTo(middleX + depth + podiumWidth, topY - depth)
            lineTo(middleX + depth + podiumWidth, topY + podiumHeight - depth)
            lineTo(middleX + podiumWidth, topY + podiumHeight)
            close()
        }, darkSecondaryShade)

        drawPath(Path().apply {
            moveTo(middleX, topY)
            lineTo(middleX + podiumWidth, topY)
            lineTo(middleX + podiumWidth + depth, topY - depth)
            lineTo(middleX - depth, topY - depth)
            close()
        }, gradientSecondary)

        drawRect(gradientSecondary, Offset(middleX, topY), Size(podiumWidth, podiumHeight))

        // 3e - Droite
        drawPath(Path().apply {
            moveTo(rightX + podiumWidth, topY + rightHeightOffset)
            lineTo(rightX + depth + podiumWidth, topY + rightHeightOffset - depth)
            lineTo(rightX + depth + podiumWidth, topY + podiumHeight - depth)
            lineTo(rightX + podiumWidth, topY + podiumHeight)
            close()
        }, darkSecondaryShade)

        drawPath(Path().apply {
            moveTo(rightX, topY + rightHeightOffset)
            lineTo(rightX + podiumWidth, topY + rightHeightOffset)
            lineTo(rightX + podiumWidth + depth, topY + rightHeightOffset - depth)
            lineTo(rightX + depth, topY + rightHeightOffset - depth)
            close()
        }, gradientSecondary)

        drawRect(gradientSecondary, Offset(rightX, topY + rightHeightOffset), Size(podiumWidth, podiumHeight - rightHeightOffset))

        // 2e - Gauche
        drawPath(Path().apply {
            moveTo(leftX, topY + leftHeightOffset)
            lineTo(leftX - depth, topY + leftHeightOffset - depth)
            lineTo(leftX - depth, topY + podiumHeight - depth)
            lineTo(leftX, topY + podiumHeight)
            close()
        }, darkSecondaryShade)

        drawPath(Path().apply {
            moveTo(leftX, topY + leftHeightOffset)
            lineTo(leftX + podiumWidth, topY + leftHeightOffset)
            lineTo(leftX + podiumWidth - depth, topY + leftHeightOffset - depth)
            lineTo(leftX - depth, topY + leftHeightOffset - depth)
            close()
        }, gradientSecondary)

        drawRect(gradientSecondary, Offset(leftX, topY + leftHeightOffset), Size(podiumWidth, podiumHeight - leftHeightOffset))

        // Cercles des joueurs
        drawCircle(gradientSecondary, iconSize / 2, Offset(middleX + podiumWidth / 2, topY - iconSize))
        drawCircle(gradientSecondary, iconSize / 2, Offset(rightX + podiumWidth / 2 + depth / 2, topY + rightHeightOffset - iconSize))
        drawCircle(gradientSecondary, iconSize / 2, Offset(leftX + podiumWidth / 2 - depth / 2, topY + leftHeightOffset - iconSize))

        // 👑 Couronne
        drawContext.canvas.nativeCanvas.drawText(
            "👑",
            middleX + podiumWidth / 2,
            topY - iconSize - 80f,
            android.graphics.Paint().apply {
                textSize = 60f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
        )

        // Noms des joueurs (Top 3)
        val paint = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 40f
            color = textColor.toArgb()
            isAntiAlias = true
            typeface = typeface
        }

        val textOffsetY = (paint.descent() + paint.ascent()) / 2

        drawContext.canvas.nativeCanvas.drawText(
            players.getOrNull(0)?.second?.pseudo ?: "1st",
            middleX + podiumWidth / 2,
            topY - iconSize - textOffsetY,
            paint
        )

        drawContext.canvas.nativeCanvas.drawText(
            players.getOrNull(1)?.second?.pseudo ?: "2rd",
            rightX + podiumWidth / 2 + depth / 2,
            topY + rightHeightOffset - iconSize - textOffsetY,
            paint
        )

        drawContext.canvas.nativeCanvas.drawText(
            players.getOrNull(2)?.second?.pseudo ?: "3nd",
            leftX + podiumWidth / 2 - depth / 2,
            topY + leftHeightOffset - iconSize - textOffsetY,
            paint
        )

        // Ombres sous les icônes
        drawOval(darkSecondaryShade, Offset(middleX + podiumWidth / 2 - iconSize * 0.35f, topY - iconSize / 5), Size(iconSize * 0.7f, iconSize * 0.1f))
        drawOval(darkSecondaryShade, Offset(rightX + podiumWidth / 2 + depth / 2 - iconSize * 0.35f, topY + rightHeightOffset - iconSize / 5), Size(iconSize * 0.7f, iconSize * 0.1f))
        drawOval(darkSecondaryShade, Offset(leftX + podiumWidth / 2 - depth / 2 - iconSize * 0.35f, topY + leftHeightOffset - iconSize / 5), Size(iconSize * 0.7f, iconSize * 0.1f))
    }
}



/**
 * Composable qui affiche le classement des joueurs sous forme de bulles scrollables.
 * Applique un fond spécial pour les 3 premiers (or, argent, bronze) et pour le joueur local.
 * Le joueur courant peut être sélectionné pour afficher des courbes ou des détails.
 *
 * @param players Liste des joueurs triés par distance (Pair<id, Player>).
 * @param showSpeed Booléen indiquant si les distances doivent être affichées avec le pseudo.
 * @param selectedPlayers Ensemble des identifiants de joueurs sélectionnés.
 * @param onPlayerClick Fonction appelée quand un joueur est cliqué (sélection).
 * @param meId Identifiant du joueur courant (pour appliquer un style spécial).
 * @param backgroundColor Couleur de fond par défaut des bulles.
 */
@Composable
fun PlayerRanking(
    players: List<Pair<Int, Player>>,
    showSpeed: Boolean,
    selectedPlayers: Set<Int>,
    onPlayerClick: (Int) -> Unit,
    meId: Int,
    backgroundColor: Color
) {
    // -------------------------
    // Styles de fond
    // -------------------------

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
                group.forEachIndexed { _, (id, player) ->
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
                                width = 2.dp,
                                color = if (selectedPlayers.contains(id)) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                shape = CircleShape
                            )
                            .padding(5.dp)
                    ) {
                        Text(
                            text = if (showSpeed)
                                "${player.pseudo} - ${player.distance.floatValue.toInt()} m"
                            else
                                player.pseudo,
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



/**
 * Composable qui trace un graphique Distance (X) / Temps (Y) pour les joueurs sélectionnés.
 * Les distances sont interpolées pour lisser les courbes. Chaque joueur est affiché avec des points et une ligne.
 *
 * @param players Liste des joueurs avec leurs distances dans le temps (timedDistance).
 * @param selectedPlayers Ensemble des identifiants de joueurs à afficher dans le graphique.
 */
@Composable
fun SpeedGraph(
    players: List<Pair<Int, Player>>,
    selectedPlayers: Set<Int>
) {
    // -------------------------
    // Couleurs et thème
    // -------------------------

    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val lineColor = if (isDark) Color.White else Color.Black
    val pointColor = colorScheme.secondary
    val labelColor = colorScheme.secondary
    val backgroundColor = colorScheme.primary

    // -------------------------
    // Interpolation des données
    // -------------------------

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

    // -------------------------
    // Échelle max pour les axes
    // -------------------------

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
            .background(color = backgroundColor, shape = RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        val graphWidth = size.width - 60f
        val graphHeight = size.height - 60f
        val originX = 40f
        val originY = size.height - 30f

        val stepY = graphHeight / maxYLabel

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

        drawLine(lineColor, Offset(originX, originY), Offset(originX, originY - graphHeight), strokeWidth = 2f)
        drawLine(lineColor, Offset(originX, originY), Offset(originX + graphWidth, originY), strokeWidth = 2f)

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


/**
 * Composable qui affiche deux boutons d’action à la fin de la partie :
 * un bouton "Game" pour rejouer ou voir les statistiques de jeu,
 * et un bouton dynamique "Next" → "Home" selon l’état d’avancement.
 *
 * @param onNextClicked Fonction appelée au premier clic sur le bouton "Next".
 * @param onContinueClicked Fonction appelée pour rejouer avec les infos joueur/room.
 * @param roomCode Code de la room actuelle.
 * @param playerId Identifiant du joueur courant.
 * @param startTime Timestamp du début de la partie.
 * @param infoMessage Message informatif affiché au-dessus des boutons.
 * @param hasClickedNext État indiquant si le bouton "Next" a été cliqué.
 * @param onResetClickedNext Fonction appelée quand "Home" est cliqué.
 */
@Composable
fun ActionButtons(
    onNextClicked: () -> Unit,
    onContinueClicked: (String, Int, Long) -> Unit,
    roomCode: String,
    playerId: Int,
    startTime: Long,
    infoMessage: String,
    hasClickedNext: Boolean,
    onResetClickedNext: () -> Unit
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .width(150.dp)
                    .height(56.dp)
            ) {
                Text(
                    text = "Game",
                    fontSize = 20.sp,
                    fontFamily = MartianMono,
                    color = Color.White
                )
            }

            Button(
                onClick = {
                    if (!hasClickedNext) {
                        onNextClicked()
                    } else {
                        onResetClickedNext()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier
                    .width(150.dp)
                    .height(56.dp)
            ) {
                Text(
                    text = if (hasClickedNext) "Home" else "Next",
                    fontSize = 20.sp,
                    fontFamily = MartianMono,
                    color = Color.White
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