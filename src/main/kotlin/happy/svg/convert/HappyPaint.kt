package happy.svg.convert

import happy.svg.HappyWheels
import path.utils.math.MatrixTransform
import path.utils.math.Transforms
import path.utils.math.Vec2
import path.utils.math.nearOrLess
import path.utils.paths.*
import path.utils.paths.rect
import path.utils.paths.ring
import path.utils.paths.transformWith
import java.awt.Color
import java.awt.LinearGradientPaint
import java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE
import java.awt.Paint as AwtPaint
import java.awt.RadialGradientPaint
import java.awt.Rectangle
import java.awt.TexturePaint
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import kotlin.contracts.contract
import kotlin.math.*

sealed interface HappyPaint {
    fun doFill(
        prefs: HappyPreferences,
        doFill: (part: Path, color: Color) -> Unit
    )
}

class HappyColor(
    val color: Color
) : HappyPaint {
    override fun doFill(prefs: HappyPreferences, doFill: (part: Path, color: Color) -> Unit) {
        doFill(rect(HappyWheels.levelBounds), color)
    }
}

class HappyLinearGradient(
    val start: Vec2,
    val end: Vec2,
    val transform: MatrixTransform,
    val stops: List<Pair<Double, Color>>
) : HappyPaint {
    override fun doFill(prefs: HappyPreferences, doFill: (part: Path, color: Color) -> Unit) {
        val length = (end - start).length
        val fullTransform = Transforms
            .rotate(atan2(end.y - start.y, end.x - start.x))
            .translate(start.x, start.y)
            .post(other = transform)

        Interpolation.doGradient(stops, length, prefs) { start, end, color ->
            val left = length * start - prefs.additionalGradientPartSize
            val right = length * end + prefs.additionalGradientPartSize
            val band = rect(left, -10000.0, right - left, 20000.0)

            doFill(band.transformWith(fullTransform), color)
        }
    }
}

class HappyRadialGradient(
    val center: Vec2,
    val focus: Vec2,
    val radius: Double,
    val transform: MatrixTransform,
    val stops: List<Pair<Double, Color>>
) : HappyPaint {
    override fun doFill(prefs: HappyPreferences, doFill: (part: Path, color: Color) -> Unit) {
        Interpolation.doGradient(stops, radius, prefs) { start, end, color ->
            val ringCenter = center * start + focus * (1 - start)
            val outRadius = max(0.0, radius * start - prefs.additionalGradientPartSize)
            val inRadius = radius * end + prefs.additionalGradientPartSize
            val ring = ring(ringCenter.x, ringCenter.y, outRadius, inRadius)

            doFill(ring.transformWith(transform), color)
        }
    }
}

class HappyTexture(
    val fillBounds: Bounds,
    val texture: AwtPaint
) : HappyPaint {
    override fun doFill(prefs: HappyPreferences, doFill: (part: Path, color: Color) -> Unit) {
        if (texture is TexturePaint) {
            val anchorRect = texture.anchorRect.run { Bounds(x, y, width, height) }

            val canMoveAndTruncate =
                (fillBounds.top % anchorRect.w + fillBounds.w nearOrLess anchorRect.w) &&
                        (fillBounds.left % anchorRect.h + fillBounds.h nearOrLess anchorRect.h)

            if (canMoveAndTruncate) {
                var image = texture.image

                return if (prefs.doVectorizing) {
                    Vectorizing.doVectorize(image, fillBounds.x, fillBounds.y, prefs.pixelSize, doFill)
                } else {
                    var width = image.width
                    var height = image.height

                    var scaleX = anchorRect.w / width
                    var scaleY = anchorRect.h / height

                    val min = min(scaleX, scaleY)
                    // if a pixel width or height is smaller than min pixel size, increase
                    // pixel sides proportionally and decrease image width and height
                    if (min < prefs.pixelSize) {
                        if (scaleX <= scaleY) {
                            scaleY = max(scaleX, scaleY) * (prefs.pixelSize / min)
                            scaleX = prefs.pixelSize
                        } else {
                            scaleX = max(scaleX, scaleY) * (prefs.pixelSize / min)
                            scaleY = prefs.pixelSize
                        }

                        width = (width / scaleX).roundToInt()
                        height = (height / scaleY).roundToInt()
                        image = image.scale(width, height).toBuffered()

                        // compute right pixel width and height
                        scaleX = anchorRect.w / width
                        scaleY = anchorRect.h / height
                    }

                    Vectorizing.doPixilate(image, fillBounds.x, fillBounds.y, scaleX, scaleY, prefs.colorCounts) { x, y, w, h, color ->
                        doFill(rect(x, y, w, h), color)
                    }
                }
            }
        }



        val levelRect = Rectangle(0, 0, 20000, 10000)
        val pathRect = fillBounds.run { Rectangle2D.Double(x, y, w, h) }.bounds
        val context = texture.createContext(ColorModel.getRGBdefault(), levelRect, levelRect, null, prefs.hints)

        val readable = pathRect.run { context.getRaster(x, y, width, height) }
        val writable = readable.createCompatibleWritableRaster()
        writable.setDataElements(0, 0, readable)
        var image = BufferedImage(context.colorModel, writable, context.colorModel.isAlphaPremultiplied, null)

        return if (prefs.doVectorizing) {
            Vectorizing.doVectorize(image, fillBounds.x, fillBounds.y, prefs.pixelSize, doFill)
        } else {
            if (prefs.pixelSize > 1.0) {
                val width = (image.width / prefs.pixelSize).roundToInt()
                val height = (image.height / prefs.pixelSize).roundToInt()
                image = image.scale(width, height).toBuffered()
            }
            val px = (fillBounds.h / image.width).coerceAtLeast(prefs.pixelSize)
            val py = (fillBounds.w / image.height).coerceAtLeast(prefs.pixelSize)

            Vectorizing.doPixilate(image, fillBounds.x, fillBounds.y, px, py, prefs.colorCounts) { x, y, w, h, color ->
                doFill(rect(x, y, w, h), color)
            }
        }
    }
}

fun AwtPaint.toHappyPaint(): HappyPaint? = when(this) {
    is Color -> HappyColor(this)

    is LinearGradientPaint -> {
        if (cycleMethod != NO_CYCLE)
            throw IllegalArgumentException("Cycle methods are not supported yet")

        HappyLinearGradient(
            start = startPoint.run { Vec2(x, y) },
            end = endPoint.run { Vec2(x, y) },
            transform = transform.toMatrixTransform(),
            stops = fractions.mapIndexed { i, p -> p.toDouble() to colors[i] }.sortedBy { it.first }
        )
    }

    is RadialGradientPaint -> {
        if (cycleMethod != NO_CYCLE)
            throw IllegalArgumentException("Cycle methods are not supported yet")

        HappyRadialGradient(
            center = centerPoint.run { Vec2(x, y) },
            focus = focusPoint.run { Vec2(x, y) },
            radius = radius.toDouble(),
            transform = transform.toMatrixTransform(),
            stops = fractions.mapIndexed { i, p -> p.toDouble() to colors[i] }.sortedBy { it.first }
        )
    }

    else -> null
}

fun AwtPaint.toHappyPaint(anchor: Bounds) = when(this) {
    is Color, is LinearGradientPaint, is RadialGradientPaint -> toHappyPaint()!!
    else -> HappyTexture(anchor, this)
}
