package happy.svg.paint

import happy.svg.HappyWheels
import happy.svg.convert.HappyPreferences
import happy.svg.convert.Interpolation
import happy.svg.convert.Pixelating
import path.utils.math.MatrixTransform
import path.utils.math.Transforms
import path.utils.math.Vec2
import path.utils.math.lerp
import path.utils.math.nearOrLess
import path.utils.paths.*
import path.utils.paths.circle
import path.utils.paths.rect
import path.utils.paths.reversePath
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
        doFill(rect(HappyWheels.LEVEL_BOUNDS), color)
    }
}

class HappyLinearGradient(
    val start: Vec2,
    val end: Vec2,
    val stops: List<Pair<Double, Color>>,
    val transform: MatrixTransform? = null,
) : HappyPaint {
    override fun doFill(prefs: HappyPreferences, doFill: (part: Path, color: Color) -> Unit) {
        val length = (end - start).length
        val fullTransform = Transforms
            .rotate(atan2(end.y - start.y, end.x - start.x))
            .translate(start.x, start.y)
            .let { transform?.pre(it) ?: it }

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
    val stops: List<Pair<Double, Color>>,
    val transform: MatrixTransform? = null,
) : HappyPaint {
    override fun doFill(prefs: HappyPreferences, doFill: (part: Path, color: Color) -> Unit) {
        Interpolation.doGradient(stops, radius, prefs) { start, end, color ->
            val (outX, outY) = lerp(focus, center, start)
            val outR = (radius * start - prefs.additionalGradientPartSize).coerceAtLeast(0.0)
            val (inX, inY) = lerp(focus, center, end)
            val inR = radius * end + prefs.additionalGradientPartSize
            val ring = circle(outX, outY, outR) + circle(inX, inY, inR).reversePath()

            doFill(transform?.let { ring.transformWith(it) } ?: ring, color)
        }
    }
}

class HappyTexture(
    val fillBounds: Bounds,
    val texture: AwtPaint
) : HappyPaint {
    override fun doFill(prefs: HappyPreferences, doFill: (part: Path, color: Color) -> Unit) {
        val px = prefs.pixelSize
        if (texture is TexturePaint) {
            val anchorRect = texture.anchorRect.run { Bounds(x, y, width, height) }

            val canMoveAndTruncate =
                (fillBounds.top % anchorRect.w + fillBounds.w nearOrLess anchorRect.w) &&
                        (fillBounds.left % anchorRect.h + fillBounds.h nearOrLess anchorRect.h)

            if (canMoveAndTruncate) {
                var image = texture.image
                var width = image.width
                var height = image.height

                var scaleX = anchorRect.w / width
                var scaleY = anchorRect.h / height

                val min = min(scaleX, scaleY)
                // if a pixel width or height is smaller than min pixel size, increase
                // pixel sides proportionally and decrease image width and height
                if (min < px) {
                    if (scaleX <= scaleY) {
                        scaleY = max(scaleX, scaleY) * (px / min)
                        scaleX = px
                    } else {
                        scaleX = max(scaleX, scaleY) * (px / min)
                        scaleY = px
                    }

                    width = (width / scaleX).roundToInt()
                    height = (height / scaleY).roundToInt()
                    image = image.scale(width, height).toBuffered()

                    // compute right pixel width and height
                    scaleX = anchorRect.w / width
                    scaleY = anchorRect.h / height
                }

                return Pixelating.doPixelate(image, fillBounds.x, fillBounds.y, scaleX, scaleY, prefs, doFill)
            }
        }

        // can't just use BufferedImage from texture => get raster from paint and pixelate it

        val levelRect = Rectangle(0, 0, 20000, 10000)
        val pathRect = fillBounds.run { Rectangle2D.Double(x, y, w, h) }.bounds
        val context = texture.createContext(ColorModel.getRGBdefault(), levelRect, levelRect, null, prefs.hints)

        val readable = pathRect.run { context.getRaster(x, y, width, height) }
        val writable = readable.createCompatibleWritableRaster().apply { setDataElements(0, 0, readable) }
        var image = BufferedImage(context.colorModel, writable, context.colorModel.isAlphaPremultiplied, null)

        if (px > 1.0) {
            val width = (image.width / px).roundToInt()
            val height = (image.height / px).roundToInt()
            image = image.scale(width, height).toBuffered()
        }

        return Pixelating.doPixelate(
            img = image,
            translateX = fillBounds.x,
            translateY = fillBounds.y,
            scaleX = (fillBounds.h / image.width).coerceAtLeast(px),
            scaleY = (fillBounds.w / image.height).coerceAtLeast(px),
            preferences = prefs,
            doFill = doFill
        )
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
