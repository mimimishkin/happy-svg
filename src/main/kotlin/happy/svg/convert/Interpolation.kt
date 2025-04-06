package happy.svg.convert

import happy.svg.HappyWheels
import java.awt.Color
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt

internal object Interpolation {
    fun interpolateInPair(left: Color, right: Color, progress: Double): Color {
        val clip = { d: Double -> d.roundToInt().coerceIn(0..255) }

        val f = (1 - cos(progress * PI)) * 0.5
        return Color(
            clip(left.red * (1 - f) + right.red * f),
            clip(left.green * (1 - f) + right.green * f),
            clip(left.blue * (1 - f) + right.blue * f),
            clip(left.alpha * (1 - f) + right.alpha * f)
        )
    }

    fun interpolateInList(stops: List<Pair<Double, Color>>, progress: Double): Color {
        val (leftBound, leftColor) = stops.findLast { it.first < progress } ?: stops[0]
        val (rightBound, rightColor) = stops.find { it.first > progress } ?: stops[stops.lastIndex]
        val subProgress = (progress - leftBound) / (rightBound - leftBound)
        return interpolateInPair(leftColor, rightColor, subProgress.coerceIn(0.0..1.0))
    }

    @PublishedApi
    internal inline fun doGradientFull(
        stops: List<Pair<Double, Color>>,
        size: Double,
        minColorDifference: Double,
        minPartSize: Double,
        processPart: (start: Double, end: Double, color: Color) -> Unit
    ) {
        val startColor = interpolateInList(stops, 0.0)
        processPart(-10000.0, 0.0, startColor)

        val step = 0.0005
        var prevProgress = 0.0
        var prevColor = startColor
        var progress = step
        val left = FloatArray(4)
        val right = FloatArray(4)
        while (progress < 1.0) {
            val color = interpolateInList(stops, progress)
            prevColor.getComponents(left)
            color.getComponents(right)
            val diff = abs(left[0] - right[0]) + abs(left[1] - right[1]) + abs(left[2] - right[2]) + abs(left[3] - right[3])
            val partSize = size * (progress - prevProgress)
            if (diff >= minColorDifference && partSize >= minPartSize) {
                processPart(prevProgress, progress, color)

                prevProgress = progress
                prevColor = color
            }

            progress += step
        }

        processPart(prevProgress, 1.0, interpolateInList(stops, 1.0))
        processPart(1.0, 10001.0, interpolateInList(stops, 1.0))
    }

    inline fun doGradient(
        stops: List<Pair<Double, Color>>,
        size: Double,
        prefs: HappyPreferences,
        processPart: (start: Double, end: Double, color: Color) -> Unit
    ) {
        doGradientFull(stops, size, prefs.minColorDifference, prefs.minGradientPartSize) { start, end, color ->
            if (color.alpha > HappyWheels.MIN_VISIBLE_ALPHA)
                processPart(start, end, color)
        }
    }
}