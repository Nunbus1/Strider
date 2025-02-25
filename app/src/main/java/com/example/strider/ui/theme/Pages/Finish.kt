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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import kotlin.random.Random

@Composable
fun FinishScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderSection()
        Spacer(modifier = Modifier.height(16.dp))
        ResultSection(playerName = "Bob", resultMessage = "Win this match")
        Spacer(modifier = Modifier.height(16.dp))
        Podium()
        Spacer(modifier = Modifier.height(16.dp))
        PlayerRanking()
        Spacer(modifier = Modifier.height(24.dp))
        ActionButtons()
    }
}

val gradientBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFF22FFFB), Color(0xFF48AAC5))
)

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier.size(32.dp).clickable { /* Action retour */ }
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
        //modifier = Modifier.padding(top = 16.dp)
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
    Canvas(modifier = Modifier.fillMaxWidth().height(300.dp)) {
        val centerX = size.width / 2
        val podiumWidth = 220f
        val podiumHeight = 440f
        val leftX = centerX - (1.5f * podiumWidth)
        val middleX = centerX - (0.5f * podiumWidth)
        val rightX = centerX + (0.5f * podiumWidth)
        val topY = 350f
        val depth = 50f
        val leftHeightOffset = 200f
        val rightHeightOffset = 100f
        val iconSize = 200f

        //-------- Middle --------//
        val middleFaceLeft = Path().apply {
            moveTo(middleX, topY)
            lineTo(middleX - depth, topY - depth)
            lineTo(middleX - depth, topY + podiumHeight - depth)
            lineTo(middleX, topY + podiumHeight)
            close()
        }
        drawPath(middleFaceLeft, gradientBrush)
        drawPath(middleFaceLeft, Color.Black, style = Stroke(width = 4f))

        val middleFaceRight = Path().apply {
            moveTo(middleX + podiumWidth, topY)
            lineTo(middleX + depth + podiumWidth, topY - depth)
            lineTo(middleX + depth + podiumWidth, topY + podiumHeight - depth)
            lineTo(middleX + podiumWidth, topY + podiumHeight)
            close()
        }
        drawPath(middleFaceRight, gradientBrush)
        drawPath(middleFaceRight, Color.Black, style = Stroke(width = 4f))

        val middleTop = Path().apply {
            moveTo(middleX, topY)
            lineTo(middleX + podiumWidth, topY)
            lineTo(middleX + podiumWidth + depth, topY - depth)
            lineTo(middleX - depth, topY - depth)
            close()
        }
        drawPath(middleTop, gradientBrush)
        drawPath(middleTop, Color.Black, style = Stroke(width = 4f))

        drawRect(gradientBrush, topLeft = Offset(middleX, topY), size = androidx.compose.ui.geometry.Size(podiumWidth, podiumHeight))
        drawRect(Color.Black, topLeft = Offset(middleX, topY), size = androidx.compose.ui.geometry.Size(podiumWidth, podiumHeight), style = Stroke(width = 4f))

        //-------- Right --------//
        val rightFace = Path().apply {
            moveTo(rightX + podiumWidth, topY + rightHeightOffset)
            lineTo(rightX + depth + podiumWidth, topY + rightHeightOffset - depth)
            lineTo(rightX + depth + podiumWidth, topY + podiumHeight - depth)
            lineTo(rightX + podiumWidth, topY + podiumHeight)
            close()
        }
        drawPath(rightFace, gradientBrush)
        drawPath(rightFace, Color.Black, style = Stroke(width = 4f))

        val rightTop = Path().apply {
            moveTo(rightX, topY + rightHeightOffset)
            lineTo(rightX + podiumWidth, topY + rightHeightOffset)
            lineTo(rightX + podiumWidth + depth, topY + rightHeightOffset - depth)
            lineTo(rightX + depth, topY + rightHeightOffset - depth)
            close()
        }
        drawPath(rightTop, gradientBrush)
        drawPath(rightTop, Color.Black, style = Stroke(width = 4f))

        drawRect(gradientBrush, topLeft = Offset(rightX, topY + rightHeightOffset), size = androidx.compose.ui.geometry.Size(podiumWidth, podiumHeight - rightHeightOffset))
        drawRect(Color.Black, topLeft = Offset(rightX, topY + rightHeightOffset), size = androidx.compose.ui.geometry.Size(podiumWidth, podiumHeight - rightHeightOffset), style = Stroke(width = 4f))

        //-------- Left --------//
        val leftFace = Path().apply {
            moveTo(leftX, topY + leftHeightOffset)
            lineTo(leftX - depth, topY + leftHeightOffset - depth)
            lineTo(leftX - depth, topY + podiumHeight - depth)
            lineTo(leftX, topY + podiumHeight)
            close()
        }
        drawPath(leftFace, gradientBrush)
        drawPath(leftFace, Color.Black, style = Stroke(width = 4f))

        val leftTop = Path().apply {
            moveTo(leftX, topY + leftHeightOffset)
            lineTo(leftX + podiumWidth, topY + leftHeightOffset)
            lineTo(leftX + podiumWidth - depth, topY + leftHeightOffset - depth)
            lineTo(leftX - depth, topY + leftHeightOffset - depth)
            close()
        }
        drawPath(leftTop, gradientBrush)
        drawPath(leftTop, Color.Black, style = Stroke(width = 4f))

        drawRect(gradientBrush, topLeft = Offset(leftX, topY + leftHeightOffset), size = androidx.compose.ui.geometry.Size(podiumWidth, podiumHeight - leftHeightOffset))
        drawRect(Color.Black, topLeft = Offset(leftX, topY + leftHeightOffset), size = androidx.compose.ui.geometry.Size(podiumWidth, podiumHeight - leftHeightOffset), style = Stroke(width = 4f))

        //-------- Icone --------//
        drawCircle(Color.Black, radius = iconSize / 2, center = Offset(middleX + podiumWidth / 2 , topY - iconSize ))
        drawCircle(Color.Black, radius = iconSize / 2, center = Offset(rightX  + podiumWidth / 2  + depth, topY + rightHeightOffset - iconSize ))
        drawCircle(Color.Black, radius = iconSize / 2, center = Offset(leftX + podiumWidth / 2  - depth, topY + leftHeightOffset - iconSize ))
    }
}

@Composable
fun PlayerRanking() {
    val meIndex = Random.nextInt(7) // Générer un index fixe pour "Me"
    val players = List(7) { if (it == meIndex) "(Me)" else "Player ${it + 1}" }
    val gradients = listOf(
        Brush.verticalGradient(colors = listOf(Color(0xFFFFD700), Color(0xFFFFE066))), // Or
        Brush.verticalGradient(colors = listOf(Color(0xFFC0C0C0), Color(0xFFD9D9D9))), // Argent
        Brush.verticalGradient(colors = listOf(Color(0xFFCD7F32), Color(0xFFD8A47F))) // Bronze
    )
    val meGradient = Brush.verticalGradient(colors = listOf(Color(0xFF22FFFB), Color(0xFF48AAC5))) // Bleu

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.Center
    ) {
        players.chunked(5).forEachIndexed { globalIndex, group ->
            Column(
                modifier = Modifier
                    .width(300.dp) // Augmenter la largeur des colonnes
                    .padding(horizontal = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                group.forEachIndexed { localIndex, player ->
                    Box(
                        modifier = Modifier
                            .width(250.dp) // Augmenter la largeur des éléments
                            .padding(5.dp)
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
                            .padding(5.dp) // Augmenter l'épaisseur de la bordure
                    ) {
                        Text(text = player, fontSize = 15.sp, color = Color.Black, modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButtons() {
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
            Text(text = "Home", fontSize = 20.sp, color = Color.White)
        }

        Button(
            onClick = { /* Action Next */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(colors = listOf(Color(0xFFFC3789), Color(0xFFFF2225))),
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
