package com.dagomusic.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.glassmorphic(
    enableBlur: Boolean = true,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    alpha: Float = 0.15f
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    val baseColor = if (enableBlur) {
        Color.White.copy(alpha = alpha)
    } else {
        Color.DarkGray.copy(alpha = 0.85f)
    }

    return this
        .clip(shape)
        .background(baseColor)
        .border(
            borderWidth,
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.3f),
                    Color.White.copy(alpha = 0.05f)
                )
            ),
            shape
        )
}
