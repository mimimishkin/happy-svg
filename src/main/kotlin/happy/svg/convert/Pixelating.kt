package happy.svg.convert

import happy.svg.HappyWheels
import happy.svg.paint.hasAlpha
import path.utils.paths.Bounds
import path.utils.paths.Operand
import path.utils.paths.Path
import path.utils.paths.rect
import path.utils.paths.toOperand
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

internal object Pixelating {
    inline fun doEveryPixel(
        img: BufferedImage,
        processPixel: (x: Int, y: Int, r: Int, g: Int, b: Int, a: Int) -> Unit
    ) {
        val raster = img.raster
        val pixels = raster.getPixels(0, 0, img.width, img.height, null as IntArray?)
        val hasAlpha = img.hasAlpha
        val bands = if (hasAlpha) 4 else 3

        for (y in 0 until img.height) {
            for (x in 0 until img.width) {
                val offset = (x + y * img.width) * bands
                if (!hasAlpha || pixels[offset + 3] > HappyWheels.MIN_VISIBLE_ALPHA) {
                    processPixel(x, y, pixels[offset], pixels[offset + 1], pixels[offset + 2], if (hasAlpha) pixels[offset + 3] else 255)
                }
            }
        }
    }

    inline fun doPixelsOptimized(
        img: BufferedImage,
        translateX: Double,
        translateY: Double,
        scaleX: Double,
        scaleY: Double,
        colorCount: Int = 32,
        extraWidth: Double = 0.0075,
        processBand: (bounds: Bounds, color: Color) -> Unit
    ) {
        var startX = 0
        var startY = 0
        val width = img.width
        var currentCount = 0
        var currentColor: Color? = null

        doEveryPixel(img) { x, y, r, g, b, a ->
            val reducedColor = Color(
                ((r / 255f * colorCount).roundToInt() / colorCount.toFloat() * 255).roundToInt(),
                ((g / 255f * colorCount).roundToInt() / colorCount.toFloat() * 255).roundToInt(),
                ((b / 255f * colorCount).roundToInt() / colorCount.toFloat() * 255).roundToInt(),
                a,
            )
            if (currentColor == null)
                currentColor = reducedColor

            if (currentColor == reducedColor) {
                currentCount += 1
            }

            if (currentColor != reducedColor || x == width - 1 && y == img.height - 1) {
                if (startX != 0 || currentCount < width * 2) {
                    processBand(Bounds(
                        x = translateX + startX * scaleX - extraWidth,
                        y = translateY + startY * scaleY - extraWidth,
                        w = currentCount.coerceAtMost(width - startX) * scaleX + extraWidth * 2,
                        h = scaleY + extraWidth * 2
                    ), currentColor)
                    currentCount -= width - startX
                    startY += 1
                }

                if (currentCount >= width * 2) {
                    val lines = currentCount / width
                    processBand(Bounds(
                        x = translateX - extraWidth,
                        y = translateY + startY * scaleY - extraWidth,
                        w = width * scaleX + extraWidth * 2,
                        h = lines * scaleY + extraWidth * 2
                    ), currentColor)
                    currentCount -= lines * width
                    startY += lines
                }

                if (currentCount > 0) {
                    processBand(Bounds(
                        x = translateX - extraWidth,
                        y = translateY + startY * scaleY - extraWidth,
                        w = currentCount * scaleX + extraWidth * 2,
                        h = scaleY + extraWidth * 2
                    ), currentColor)
                }

                startX = x
                startY = y
                currentCount = 1
                currentColor = reducedColor
            }
        }
    }

    fun mergePixels(
        img: BufferedImage,
        translateX: Double,
        translateY: Double,
        scaleX: Double,
        scaleY: Double,
        colorCount: Int = 16,
    ): List<Pair<Path, Color>> {
        val parts = mutableMapOf<Color, Operand>()

        doPixelsOptimized(img, translateX, translateY, scaleX, scaleY, colorCount) { band, color ->
            val current = rect(band).toOperand()
            val accumulated = parts[color]
            if (accumulated == null) {
                parts.put(color, current)
            } else {
                parts.put(color, accumulated union current)
            }
        }

        return parts.map { Pair(it.value.toPath(), it.key) }
    }

    inline fun doPixelate(
        img: BufferedImage,
        translateX: Double,
        translateY: Double,
        scaleX: Double,
        scaleY: Double,
        preferences: HappyPreferences,
        doFill: (part: Path, color: Color) -> Unit
    ) {
        if (preferences.mergePixels) {
            mergePixels(img, translateX, translateY, scaleX, scaleY, preferences.colorCounts).forEach { (part, color) ->
                doFill(part, color)
            }
        } else {
            doPixelsOptimized(img, translateX, translateY, scaleX, scaleY, preferences.colorCounts) { bounds, color ->
                doFill(rect(bounds), color)
            }
        }
    }
}
