package com.example.qty.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qty.pricedynamics.trend.TrendDirection
import com.example.ui.theme.TelemetryAmber
import com.example.ui.theme.TelemetryCardBorder
import com.example.ui.theme.TelemetryCyan
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TelemetryObsidian
import com.example.ui.theme.TelemetryRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.util.Locale

/**
 * High-precision canvas displaying authentic BTC price points and the OLS regression trend line.
 * Strictly plots verified authentic points without synthetic interpolation.
 */
@Composable
fun TrendCanvas(
    points: List<Pair<Long, Double>>,
    slopePerSecond: Double,
    rSquared: Double,
    direction: TrendDirection,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(TelemetryObsidian, RoundedCornerShape(12.dp))
            .border(1.dp, TelemetryCardBorder, RoundedCornerShape(12.dp))
            .padding(8.dp)
            .testTag("trend_canvas_box")
    ) {
        if (points.size < 2) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Awaiting authentic chronological points (N=${points.size})...",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            return
        }

        val minPrice = points.minOf { it.second }
        val maxPrice = points.maxOf { it.second }
        val minTime = points.minOf { it.first }
        val maxTime = points.maxOf { it.first }

        val priceSpan = (maxPrice - minPrice).coerceAtLeast(1.0)
        val timeSpan = (maxTime - minTime).coerceAtLeast(1L).toDouble()

        val trendColor = when (direction) {
            TrendDirection.UP -> TelemetryGreen
            TrendDirection.DOWN -> TelemetryRed
            TrendDirection.NO_TREND -> TelemetryCyan
            TrendDirection.FAIL_CLOSED -> TelemetryAmber
        }

        Canvas(modifier = Modifier.fillMaxSize().testTag("trend_canvas")) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 1. Telemetry Grid lines (3 horizontal lines)
            val gridStroke = Stroke(
                width = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
            val gridColor = Color(0x2200E5FF)

            for (i in 1..3) {
                val y = canvasHeight * (i / 4f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), 0f)
                )
            }

            // Map (timestamp, price) to Canvas (x, y)
            fun getX(t: Long): Float {
                return (((t - minTime).toDouble() / timeSpan) * canvasWidth).toFloat()
            }

            fun getY(p: Double): Float {
                val normalized = (p - minPrice) / priceSpan
                // Invert Y so highest price is at top (y = 0)
                // Add 10% vertical padding
                val padY = canvasHeight * 0.12f
                val effectiveHeight = canvasHeight - (2 * padY)
                return (canvasHeight - padY - (normalized * effectiveHeight)).toFloat()
            }

            // 2. Plot Authentic Price Path
            val path = Path()
            val fillPath = Path()

            points.forEachIndexed { idx, pt ->
                val x = getX(pt.first)
                val y = getY(pt.second)
                if (idx == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, canvasHeight)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }
            fillPath.lineTo(getX(points.last().first), canvasHeight)
            fillPath.close()

            // Subtle gradient area under price line
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        trendColor.copy(alpha = 0.18f),
                        trendColor.copy(alpha = 0.01f)
                    ),
                    startY = 0f,
                    endY = canvasHeight
                )
            )

            // Price Stroke Line
            drawPath(
                path = path,
                color = trendColor.copy(alpha = 0.9f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // 3. OLS Linear Regression Line Overlay
            // y_start = p0_pred, y_end = pN_pred
            // Using exact OLS: predicted = meanP + slope * (t - meanT)
            val meanT = points.map { it.first }.average()
            val meanP = points.map { it.second }.average()

            val tStartSec = (minTime - meanT) / 1000.0
            val tEndSec = (maxTime - meanT) / 1000.0

            val predPriceStart = meanP + slopePerSecond * tStartSec
            val predPriceEnd = meanP + slopePerSecond * tEndSec

            val startOffset = Offset(getX(minTime), getY(predPriceStart))
            val endOffset = Offset(getX(maxTime), getY(predPriceEnd))

            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = startOffset,
                end = endOffset,
                strokeWidth = 2.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f),
                cap = StrokeCap.Round
            )

            // 4. Highlight Last Price Point
            val lastX = getX(points.last().first)
            val lastY = getY(points.last().second)
            drawCircle(
                color = trendColor,
                radius = 4.5.dp.toPx(),
                center = Offset(lastX, lastY)
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = Offset(lastX, lastY)
            )
        }

        // Overlay Price Bounds Labels
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = String.format(Locale.US, "$%.2f", maxPrice),
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.TopStart)
            )
            Text(
                text = String.format(Locale.US, "$%.2f", minPrice),
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.BottomStart)
            )
            Text(
                text = String.format(Locale.US, "OLS Slope Trend (R² = %.2f)", rSquared),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}
