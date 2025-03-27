package happy.svg.paint

import com.kitfox.svg.SVGDiagram
import com.kitfox.svg.SVGUniverse
import happy.svg.HappyLayer
import happy.svg.HappyWheels.Collision
import happy.svg.convert.HappyPreferences
import path.utils.math.Transforms
import path.utils.math.near
import path.utils.paths.*
import java.awt.Color
import java.awt.TexturePaint
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.io.File
import java.io.Reader
import java.net.URI
import java.net.URL
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

inline fun HappyPaint.fill(
    path: Path?,
    prefs: HappyPreferences,
    crossinline doFill: (part: Path, color: Color) -> Unit
) {
    require(path != null || this is HappyTexture) { "Path is required to paint along it" }

    if (this is HappyColor)
        return doFill(path!!, color)

    doFill(prefs) { part, color ->
        doFill(if (path != null) part and path else part, color)
    }
}

fun HappyLayer.possiblyInteractiveShape(
    path: Path,
    color: Color = this.color,
    outline: Color? = this.outline,
    isInteractive: Boolean = this.isInteractive,
    isFixed: Boolean = this.isFixed,
    isSleeping: Boolean = this.isSleeping,
    density: Float = this.density,
    collision: Collision = this.collision
) {
    if (isInteractive) {
        polygon(path, color, outline, isFixed, isSleeping, density, collision, path.bounds)
    } else {
        art(path, color, outline, path.bounds)
    }
}

fun HappyLayer.possiblyInteractiveShape(
    path: Path,
    paint: HappyPaint,
    isInteractive: Boolean = this.isInteractive,
    isFixed: Boolean = this.isFixed,
    isSleeping: Boolean = this.isSleeping,
    density: Float = this.density,
    collision: Collision = this.collision
) {
    paint.fill(path, preferences) { part, color ->
        possiblyInteractiveShape(part, color, null, isInteractive, isFixed, isSleeping, density, collision)
    }
}

fun HappyLayer.rectangle(
    bounds: Bounds,
    paint: HappyPaint,
    isInteractive: Boolean = this.isInteractive,
    isFixed: Boolean = this.isFixed,
    isSleeping: Boolean = this.isSleeping,
    density: Float = this.density,
    collision: Collision = this.collision
) {
    possiblyInteractiveShape(rect(bounds), paint, isInteractive, isFixed, isSleeping, density, collision)
}

fun HappyLayer.circle(
    bounds: Bounds,
    paint: HappyPaint,
    isInteractive: Boolean = this.isInteractive,
    isFixed: Boolean = this.isFixed,
    isSleeping: Boolean = this.isSleeping,
    density: Float = this.density,
    collision: Collision = this.collision,
    innerCutout: Float = this.innerCutout
) {
    fun simpleRingSector(cx: Double, cy: Double, outRadius: Double, innerRadius: Double, start: Double, end: Double) = mutablePath()
        .moveTo(cx + outRadius * cos(start), cy + outRadius * sin(start))
//        .arcTo(outRadius, outRadius, 0.0, false, true, cx + outRadius * cos(end), cy + outRadius * sin(end))
        .lineTo(cx + outRadius * cos(end), cy + outRadius * sin(end))
        .lineTo(cx + innerRadius * cos(end), cy + innerRadius * sin(end))
//        .arcTo(innerRadius, innerRadius, 0.0, false, false, cx + innerRadius * cos(start), cy + innerRadius * sin(start))
        .lineTo(cx + innerRadius * cos(start), cy + innerRadius * sin(start))
        .close()

    require(bounds.w near bounds.h) { "Can't draw ellipse, only circles" }

    val radius = bounds.w / 2
    if (innerCutout == 0f) {
        val circle = circle(bounds.cx, bounds.cy, radius)
        possiblyInteractiveShape(circle, paint, isInteractive, isFixed, isSleeping, density, collision)
    } else {
        val rightScale = innerCutout / 100 * (1 - 0.015)
        val outRadius = rightScale * radius

        if (!isInteractive) {
            val ring = ring(bounds.cx, bounds.cy, radius, outRadius)
            art(ring, paint)
        } else {
            val sectorsCount = (2 * PI * radius / (preferences.minCurveLength - 0.5)).toInt() // add -0.5 to guarantee turning into lines
            val step = 2 * PI / sectorsCount
            for (i in 0 until sectorsCount) {
                val start = i * step
                val end = (i + 1) * step
                val sector = simpleRingSector(bounds.cx, bounds.cy, radius, outRadius, start, end)

                polygon(sector, paint, isFixed, isSleeping, density, collision)
            }
        }
    }
}

fun HappyLayer.triangle(
    bounds: Bounds,
    paint: HappyPaint,
    isInteractive: Boolean = this.isInteractive,
    isFixed: Boolean = this.isFixed,
    isSleeping: Boolean = this.isSleeping,
    density: Float = this.density,
    collision: Collision = this.collision
) {
    val triangle = mutablePath()
        .moveTo(bounds.left, bounds.bottom)
        .lineTo(bounds.cx, bounds.top)
        .lineTo(bounds.right, bounds.bottom)
        .close()

    possiblyInteractiveShape(triangle, paint, isInteractive, isFixed, isSleeping, density, collision)
}

fun HappyLayer.polygon(
    path: Path,
    paint: HappyPaint,
    isFixed: Boolean = this.isFixed,
    isSleeping: Boolean = this.isSleeping,
    density: Float = this.density,
    collision: Collision = this.collision
) {
    possiblyInteractiveShape(path, paint, true, isFixed, isSleeping, density, collision)
}

fun HappyLayer.art(
    path: Path,
    paint: HappyPaint,
) {
    possiblyInteractiveShape(path, paint, false)
}

fun HappyLayer.picture(
    image: BufferedImage,
    anchor: Bounds? = null,
) {
    val anchor = anchor ?: image.run { Bounds(0.0, 0.0, width.toDouble(), height.toDouble()) }
    val texture = TexturePaint(image, anchor.run { Rectangle2D.Double(x, y, w, h) })
    val paint = texture.toHappyPaint(anchor)
    paint.fill(null, preferences) { part, color ->
        art(part, color)
    }
}

fun HappyLayer.picture(
    image: SVGDiagram,
    bounds: Bounds? = null,
) {
    val viewport = image.viewRect.run { Bounds(x, y, width, height) }
    if (bounds != null) {
        transform(Transforms.rectToRect(viewport, bounds)) {
            image.render(HappyGraphics(this))
        }
    } else {
        image.render(HappyGraphics(this))
    }
}

private val universe = SVGUniverse()

internal fun SVGDiagram(reader: Reader): SVGDiagram {
    val uri = universe.loadSVG(reader, UUID.randomUUID().toString())
    return universe.getDiagram(uri)
}

fun HappyLayer.picture(
    image: File,
    bounds: Bounds? = null,
    isSvg: Boolean = true
) {
    if (isSvg) {
        picture(SVGDiagram(image.reader()), bounds)
    } else {
        picture(ImageIO.read(image), bounds)
    }
}

fun HappyLayer.picture(
    image: URL,
    bounds: Bounds? = null,
    isSvg: Boolean = true
) {
    val inputStream = image.openStream()
    if (isSvg) {
        picture(SVGDiagram(inputStream.reader()), bounds)
    } else {
        picture(ImageIO.read(inputStream), bounds)
    }
}

fun HappyLayer.picture(
    image: String,
    bounds: Bounds? = null,
    isSvg: Boolean = true
) {
    val file = File(image)
    if (file.exists()) {
        picture(file, bounds, isSvg)
    }

    val url = runCatching { URI.create(image).toURL() }
    if (url.isSuccess) {
        picture(url.getOrThrow(), bounds, isSvg)
    }

    throw IllegalArgumentException("Can't load image from $image")
}