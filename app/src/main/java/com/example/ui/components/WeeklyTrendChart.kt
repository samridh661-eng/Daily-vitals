package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.data.model.DailyRecord
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun WeeklyTrendChart(
    records: List<DailyRecord>,
    metricType: MetricType,
    modifier: Modifier = Modifier
) {
    // Reverse record order so that oldest is on the left and newest is on the right
    val sortedRecords = records.sortedBy { it.date }.takeLast(7)
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = when (metricType) {
        MetricType.STEPS -> com.example.ui.theme.StepCountGreen
        MetricType.SLEEP -> com.example.ui.theme.SleepIndigo
        MetricType.WATER -> com.example.ui.theme.WaterBlue
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (sortedRecords.isEmpty()) {
            // Safe fallback
            return@Box
        }

        Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            val width = size.width
            val height = size.height
            val paddingLeft = 30.dp.toPx()
            val paddingBottom = 20.dp.toPx()
            val chartWidth = width - paddingLeft
            val chartHeight = height - paddingBottom

            val maxVal = when (metricType) {
                MetricType.STEPS -> {
                    val maxRecord = sortedRecords.maxOfOrNull { it.steps } ?: 8000
                    (maxRecord.coerceAtLeast(8000) * 1.15f)
                }
                MetricType.SLEEP -> {
                    val maxRecord = sortedRecords.maxOfOrNull { it.sleepMinutes } ?: 450
                    (maxRecord.coerceAtLeast(450) * 1.15f)
                }
                MetricType.WATER -> {
                    val maxRecord = sortedRecords.maxOfOrNull { it.water } ?: 2000
                    (maxRecord.coerceAtLeast(2000) * 1.15f)
                }
            }

            // Draw Y-axis guide lines (3 guides)
            val gridColor = onSurfaceColor.copy(alpha = 0.08f)
            for (i in 0..2) {
                val y = chartHeight * (i / 2f)
                drawLine(
                    color = gridColor,
                    start = Offset(paddingLeft, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )

                // Label for values
                val labelVal = (maxVal * (1f - (i / 2f))).toInt()
                val labelStr = when (metricType) {
                    MetricType.STEPS -> if (labelVal >= 1000) "${labelVal / 1000}k" else "$labelVal"
                    MetricType.SLEEP -> String.format(Locale.getDefault(), "%.1fh", labelVal / 60f)
                    MetricType.WATER -> "${labelVal}ml"
                }

                drawContext.canvas.nativeCanvas.drawText(
                    labelStr,
                    5f,
                    y + 4.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = onSurfaceColor.copy(alpha = 0.4f).hashCode()
                        textSize = 9.dp.toPx()
                        isAntiAlias = true
                    }
                )
            }

            // Draw data
            val stepX = chartWidth / 6f // 7 items -> 6 intervals
            val points = sortedRecords.mapIndexed { index, record ->
                val x = paddingLeft + index * stepX
                val value = when (metricType) {
                    MetricType.STEPS -> record.steps.toFloat()
                    MetricType.SLEEP -> record.sleepMinutes.toFloat()
                    MetricType.WATER -> record.water.toFloat()
                }
                val y = chartHeight - (value / maxVal) * chartHeight
                Offset(x, y.coerceIn(0f, chartHeight))
            }

            if (metricType == MetricType.STEPS || metricType == MetricType.WATER) {
                // Bar Chart rendering for steps and water
                val barWidth = 14.dp.toPx()
                sortedRecords.forEachIndexed { index, record ->
                    val x = paddingLeft + index * stepX
                    val value = when (metricType) {
                        MetricType.STEPS -> record.steps.toFloat()
                        MetricType.WATER -> record.water.toFloat()
                        else -> 0f
                    }
                    val barHeight = (value / maxVal) * chartHeight
                    val y = chartHeight - barHeight

                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.85f),
                        topLeft = Offset(x - barWidth / 2f, y),
                        size = Size(barWidth, barHeight.coerceAtLeast(4.dp.toPx())),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            } else {
                // Sleep Trend: Line chart with smooth gradient curve
                val path = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            val pPrev = points[i - 1]
                            val pCurr = points[i]
                            val cp1X = pPrev.x + (pCurr.x - pPrev.x) / 2f
                            cubicTo(cp1X, pPrev.y, cp1X, pCurr.y, pCurr.x, pCurr.y)
                        }
                    }
                }

                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Fill under the sleep path
                val fillPath = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points[0].x, chartHeight)
                        lineTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            val pPrev = points[i - 1]
                            val pCurr = points[i]
                            val cp1X = pPrev.x + (pCurr.x - pPrev.x) / 2f
                            cubicTo(cp1X, pPrev.y, cp1X, pCurr.y, pCurr.x, pCurr.y)
                        }
                        lineTo(points.last().x, chartHeight)
                        close()
                    }
                }
                drawPath(
                    path = fillPath,
                    color = primaryColor.copy(alpha = 0.15f)
                )

                // Draw dots at junctions
                points.forEach { pt ->
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = onSurfaceColor,
                        radius = 2.dp.toPx(),
                        center = pt
                    )
                }
            }

            // Draw X-axis Day Labels
            val dateParser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dayLabelFormatter = SimpleDateFormat("EEE", Locale.getDefault()) // "Mon", "Tue"...
            sortedRecords.forEachIndexed { index, record ->
                val x = paddingLeft + index * stepX
                val label = try {
                    val date = dateParser.parse(record.date)
                    if (date != null) {
                        dayLabelFormatter.format(date).take(1) // Single character representation: "M", "T"
                    } else "•"
                } catch (e: Exception) {
                    "•"
                }

                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    x,
                    height,
                    android.graphics.Paint().apply {
                        color = onSurfaceColor.copy(alpha = 0.5f).hashCode()
                        textSize = 10.dp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                )
            }
        }
    }
}

enum class MetricType {
    STEPS, WATER, SLEEP
}
