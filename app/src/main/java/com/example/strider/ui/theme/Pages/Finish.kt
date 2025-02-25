package com.example.strider.ui.theme.Pages

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.strider.ui.theme.StriderTheme
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlin.random.Random
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size


@Composable
fun FinishScreen() {
    var showSpeedState by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderSection(onBackClicked = { showSpeedState = false})
        Spacer(modifier = Modifier.height(10.dp))
        ResultSection(playerName = "Bob", resultMessage = if (showSpeedState) "The fastest" else "Win this match")
        Spacer(modifier = Modifier.height(10.dp))
        if (showSpeedState) {
            SpeedBarPlot()
        } else {
            Podium()
        }
        Spacer(modifier = Modifier.height(10.dp))
        PlayerRanking(showSpeedState)
        Spacer(modifier = Modifier.height(10.dp))
        ActionButtons { showSpeedState = true }
    }
}

val gradientBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFF22FFFB), Color(0xFF48AAC5))
)

@Composable
fun HeaderSection(onBackClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier.size(32.dp).clickable { onBackClicked() }
        )
        Text(
            text = "Strider",
            fontSize = 75.sp,
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
fun PlayerRanking(showSpeed: Boolean) {
    val meIndex = Random.nextInt(12)
    val players = List(12) { if (it == meIndex) "Player ${it + 1} (Me)" else "Player ${it + 1}" }
    val speeds = List(12) { Random.nextInt(5, 20) } // Vitesse aléatoire entre 5 et 20 km/h

    val gradients = listOf(
        Brush.verticalGradient(colors = listOf(Color(0xFFFFD700), Color(0xFFFFE066))), // Or
        Brush.verticalGradient(colors = listOf(Color(0xFFC0C0C0), Color(0xFFD9D9D9))), // Argent
        Brush.verticalGradient(colors = listOf(Color(0xFFCD7F32), Color(0xFFD8A47F))) // Bronze
    )

    val meGradient = Brush.verticalGradient(colors = listOf(Color(0xFF22FFFB), Color(0xFF48AAC5)))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.Center
    ) {
        players.chunked(5).forEachIndexed { globalIndex, group ->
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .padding(horizontal = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                group.forEachIndexed { localIndex, player ->
                    val speed = speeds[globalIndex * 5 + localIndex]
                    Box(
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxWidth()
                            .shadow(8.dp, shape = RoundedCornerShape(16.dp))
                            .background(
                                when {
                                    player.contains("(Me)") -> meGradient // Bleu pour "Me"
                                    globalIndex * 5 + localIndex == 0 -> gradients[0] // Or pour le premier
                                    globalIndex * 5 + localIndex == 1 -> gradients[1] // Argent pour le deuxième
                                    globalIndex * 5 + localIndex == 2 -> gradients[2] // Bronze pour le troisième
                                    else -> Brush.verticalGradient(colors = listOf(Color.White, Color.White))
                                },
                                shape = CircleShape
                            )
                            .padding(5.dp)
                    ) {
                        Text(
                            text = if (showSpeed) "$player - ${speed}km/h" else player,
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
fun SpeedBarPlot() {
    Column(
        modifier = Modifier
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val speeds = List(5) { Random.nextInt(5, 20) }
        speeds.forEachIndexed { index, speed ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Player ${index + 1}", fontSize = 20.sp, color = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(24.dp)
                        .background(
                            Brush.horizontalGradient(colors = listOf(Color(0xFF22DCD9), Color(0xFF4398AF))),
                            shape = RoundedCornerShape(12.dp)
                        )
                )
            }
        }
    }
}


@Composable
fun ActionButtons(onNextClicked: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = { /* Action Continue */ },
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
        FinishScreen()
    }
}
