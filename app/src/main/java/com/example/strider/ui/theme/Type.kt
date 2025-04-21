package com.example.strider.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import com.example.strider.R


// -------------------------
// Font Families
// -------------------------

/**
 * Famille de police Bricolage Grotesque.
 * Utilisée pour les titres, headers, éléments visuellement forts.
 */
val BricolageGrotesque = FontFamily(
    Font(R.font.bricolage_grotesque_regular),
    Font(R.font.bricolage_grotesque_bold, FontWeight.Bold)
)

/**
 * Famille de police Martian Mono.
 * Utilisée pour les textes fonctionnels, identifiants, ou chiffres.
 */
val MartianMono = FontFamily(
    Font(R.font.martian_mono_regular),
    Font(R.font.martian_mono_bold, FontWeight.Bold)
)

// -------------------------
// Système typographique global
// -------------------------

/**
 * Définit les styles de texte utilisés dans toute l'application Strider,
 * selon les conventions Material 3.
 */
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = BricolageGrotesque,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = BricolageGrotesque,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = BricolageGrotesque,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = MartianMono,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MartianMono,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)