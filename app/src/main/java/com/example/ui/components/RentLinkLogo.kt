package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun RentLinkLogoCard(
    modifier: Modifier = Modifier,
    elevation: Boolean = true
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(
                if (elevation) 12.dp else 0.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00BBFF), // RentLinkPrimary
                        Color(0xFF1976D3)  // RentLinkSecondary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        RentLinkLogoIcon(
            modifier = Modifier
                .fillMaxSize(0.7f)
        )
    }
}

@Composable
fun RentLinkLogoIcon(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        val w = size.width
        val h = size.height

        // --- DRAW THE HOUSE STRUCTURE ---
        // Draw the white house body and roof
        val housePath = Path().apply {
            // Roof peaks
            moveTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.15f, h * 0.42f)
            lineTo(w * 0.28f, h * 0.42f)
            lineTo(w * 0.28f, h * 0.70f)
            lineTo(w * 0.72f, h * 0.70f)
            lineTo(w * 0.72f, h * 0.42f)
            lineTo(w * 0.85f, h * 0.42f)
            close()
        }
        drawPath(
            path = housePath,
            color = Color.White
        )

        // Draw the chimney
        val chimneyPath = Path().apply {
            moveTo(w * 0.65f, h * 0.27f)
            lineTo(w * 0.65f, h * 0.18f)
            lineTo(w * 0.72f, h * 0.18f)
            lineTo(w * 0.72f, h * 0.33f)
            close()
        }
        drawPath(
            path = chimneyPath,
            color = Color.White
        )

        // --- DRAW WINDOW PANES ---
        // 4 square panes in the center of the house
        val paneSize = w * 0.08f
        val startX1 = w * 0.40f
        val startX2 = w * 0.52f
        val startY1 = h * 0.42f
        val startY2 = h * 0.52f
        val windowColor = Color(0xFF00BBFF) // Sky blue

        // Pane Top-Left
        drawRoundRect(
            color = windowColor,
            topLeft = Offset(startX1, startY1),
            size = Size(paneSize, paneSize),
            cornerRadius = CornerRadius(4f, 4f)
        )
        // Pane Top-Right
        drawRoundRect(
            color = windowColor,
            topLeft = Offset(startX2, startY1),
            size = Size(paneSize, paneSize),
            cornerRadius = CornerRadius(4f, 4f)
        )
        // Pane Bottom-Left
        drawRoundRect(
            color = windowColor,
            topLeft = Offset(startX1, startY2),
            size = Size(paneSize, paneSize),
            cornerRadius = CornerRadius(4f, 4f)
        )
        // Pane Bottom-Right
        drawRoundRect(
            color = windowColor,
            topLeft = Offset(startX2, startY2),
            size = Size(paneSize, paneSize),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // --- DRAW DUAL INTERLOCKING FINTECH LINK RINGS (At the bottom) ---
        // Left Ring (Sky Blue - #00BBFF), Right Ring (Deep Blue - #1976D3)
        // They are pill-shaped/horizontal interlocking chain links
        
        val strokeWidth = w * 0.08f
        val linkWidth = w * 0.38f
        val linkHeight = h * 0.22f
        
        // Left loop top-left
        val leftTopLeft = Offset(w * 0.11f, h * 0.62f)
        // Right loop top-left
        val rightTopLeft = Offset(w * 0.51f, h * 0.62f)

        // Draw Left Ring (Sky Blue)
        drawRoundRect(
            color = Color(0xFF00BBFF),
            topLeft = leftTopLeft,
            size = Size(linkWidth, linkHeight),
            cornerRadius = CornerRadius(linkHeight * 0.5f, linkHeight * 0.5f),
            style = Stroke(width = strokeWidth)
        )

        // Draw Right Ring (Deep Blue) overlapping and interlinked
        drawRoundRect(
            color = Color(0xFF1976D3),
            topLeft = rightTopLeft,
            size = Size(linkWidth, linkHeight),
            cornerRadius = CornerRadius(linkHeight * 0.5f, linkHeight * 0.5f),
            style = Stroke(width = strokeWidth)
        )
    }
}
