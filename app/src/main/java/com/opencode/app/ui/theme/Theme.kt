package com.opencode.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// --- M3 Expressive Color Palettes ---
private val LightExpressive = lightColorScheme(
    primary = Color(0xFF65558F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF1F0F47),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1E192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFEF7FF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFEF7FF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = Color(0xFFCFBDFE),
    surfaceDim = Color(0xFFDED8E1),
    surfaceBright = Color(0xFFFEF7FF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F1F9),
    surfaceContainer = Color(0xFFF2ECF3),
    surfaceContainerHigh = Color(0xFFECE6EE),
    surfaceContainerHighest = Color(0xFFE6E0E8),
)

private val DarkExpressive = darkColorScheme(
    primary = Color(0xFFCFBDFE),
    onPrimary = Color(0xFF36265F),
    primaryContainer = Color(0xFF4C3D76),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCBC2DB),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF494458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF4A2532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = Color(0xFF65558F),
    surfaceDim = Color(0xFF141316),
    surfaceBright = Color(0xFF3B383B),
    surfaceContainerLowest = Color(0xFF0F0E11),
    surfaceContainerLow = Color(0xFF1C1A1E),
    surfaceContainer = Color(0xFF211F23),
    surfaceContainerHigh = Color(0xFF2B292D),
    surfaceContainerHighest = Color(0xFF363438),
)

// M3E Shapes: cut corner on extraSmall creates intentional "visual tension"
private val ExpressiveShapes = Shapes(
    extraSmall = CutCornerShape(6.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(40.dp),
)

// M3E Z-index scale
object ExpressiveZIndex {
    const val BASE = 0
    const val RAISED = 10
    const val STICKY = 20
    const val DROPDOWN = 30
    const val MENU = 50
    const val TOOLTIP = 55
    const val MODAL = 60
    const val SNACKBAR = 70
}

// M3E Motion values
object ExpressiveMotion {
    const val EMPHASIZED_DECELERATE = 400L
    const val EMPHASIZED_ACCELERATE = 200L
    const val SPRING_FAST = 350L
    const val SPRING_DEFAULT = 450L
    val EMPHASIZED_EASING = androidx.compose.ui.util.lerp(0.2f, 0f, 0f, 1f)
}

private fun expressiveTypography(): Typography {
    val base = Typography()
    val family = FontFamily.Default
    return base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = family, fontWeight = FontWeight.Black),
        displayMedium = base.displayMedium.copy(fontFamily = family, fontWeight = FontWeight.Black),
        displaySmall = base.displaySmall.copy(fontFamily = family, fontWeight = FontWeight.ExtraBold),
        headlineLarge = base.headlineLarge.copy(fontFamily = family, fontWeight = FontWeight.ExtraBold),
        headlineMedium = base.headlineMedium.copy(fontFamily = family, fontWeight = FontWeight.Bold),
        headlineSmall = base.headlineSmall.copy(fontFamily = family, fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontFamily = family, fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontFamily = family, fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.copy(fontFamily = family, fontWeight = FontWeight.Medium),
        bodyLarge = base.bodyLarge.copy(fontFamily = family),
        bodyMedium = base.bodyMedium.copy(fontFamily = family),
        bodySmall = base.bodySmall.copy(fontFamily = family),
        labelLarge = base.labelLarge.copy(fontFamily = family, fontWeight = FontWeight.Medium),
        labelMedium = base.labelMedium.copy(fontFamily = family, fontWeight = FontWeight.Medium),
        labelSmall = base.labelSmall.copy(fontFamily = family),
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OpenCodeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkExpressive
        else -> LightExpressive
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(shapes = ExpressiveShapes),
        typography = expressiveTypography(),
        shapes = ExpressiveShapes,
        content = content,
    )
}

/** True when the user has disabled animations in Accessibility settings. */
val LocalReduceMotion = staticCompositionLocalOf { false }
