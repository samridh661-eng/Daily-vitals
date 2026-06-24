package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SleepIndigo
import com.example.ui.theme.SleepTrackColor
import com.example.ui.theme.StepCountGreen
import com.example.ui.theme.StepTrackColor
import com.example.ui.theme.WaterBlue
import com.example.ui.theme.WaterTrackColor

@Composable
fun ConcentricMetricRings(
    stepsProgress: Float, // 0.0 to 1.0+
    waterProgress: Float,
    sleepProgress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val animatedSteps by animateFloatAsState(
        targetValue = stepsProgress.coerceAtLeast(0f),
        animationSpec = tween(1200)
    )
    val animatedWater by animateFloatAsState(
        targetValue = waterProgress.coerceAtLeast(0f),
        animationSpec = tween(1200, delayMillis = 150)
    )
    val animatedSleep by animateFloatAsState(
        targetValue = sleepProgress.coerceAtLeast(0f),
        animationSpec = tween(1200, delayMillis = 300)
    )

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 14.dp.toPx()
            val spacing = 6.dp.toPx()
            val canvasSize = this.size.width

            // Outer Ring: Steps
            val radiusOuter = (canvasSize - strokeWidth) / 2f
            drawCircle(
                color = StepTrackColor,
                radius = radiusOuter,
                center = Offset(canvasSize / 2f, canvasSize / 2f),
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = StepCountGreen,
                startAngle = -90f,
                sweepAngle = (animatedSteps * 360f).coerceIn(-360f, 360f),
                useCenter = false,
                topLeft = Offset((canvasSize - radiusOuter * 2f) / 2f, (canvasSize - radiusOuter * 2f) / 2f),
                size = Size(radiusOuter * 2f, radiusOuter * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Middle Ring: Water
            val radiusMiddle = radiusOuter - strokeWidth - spacing
            drawCircle(
                color = WaterTrackColor,
                radius = radiusMiddle,
                center = Offset(canvasSize / 2f, canvasSize / 2f),
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = WaterBlue,
                startAngle = -90f,
                sweepAngle = (animatedWater * 360f).coerceIn(-360f, 360f),
                useCenter = false,
                topLeft = Offset((canvasSize - radiusMiddle * 2f) / 2f, (canvasSize - radiusMiddle * 2f) / 2f),
                size = Size(radiusMiddle * 2f, radiusMiddle * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Inner Ring: Sleep
            val radiusInner = radiusMiddle - strokeWidth - spacing
            drawCircle(
                color = SleepTrackColor,
                radius = radiusInner,
                center = Offset(canvasSize / 2f, canvasSize / 2f),
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = SleepIndigo,
                startAngle = -90f,
                sweepAngle = (animatedSleep * 360f).coerceIn(-360f, 360f),
                useCenter = false,
                topLeft = Offset((canvasSize - radiusInner * 2f) / 2f, (canvasSize - radiusInner * 2f) / 2f),
                size = Size(radiusInner * 2f, radiusInner * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun IndividualMetricRing(
    progress: Float,
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceAtLeast(0f),
        animationSpec = tween(1000)
    )

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val canvasSize = this.size.width
            val radius = (canvasSize - strokePx) / 2f

            drawCircle(
                color = trackColor,
                radius = radius,
                center = Offset(canvasSize / 2f, canvasSize / 2f),
                style = Stroke(width = strokePx)
            )

            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = (animatedProgress * 360f).coerceIn(-360f, 360f),
                useCenter = false,
                topLeft = Offset((canvasSize - radius * 2f) / 2f, (canvasSize - radius * 2f) / 2f),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
    }
}
