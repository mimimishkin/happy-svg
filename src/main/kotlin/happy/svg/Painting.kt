package happy.svg

import com.kitfox.svg.SVGDiagram
import com.kitfox.svg.SVGUniverse
import happy.svg.HappyWheels.Collision
import happy.svg.convert.HappyColor
import happy.svg.convert.HappyPaint
import happy.svg.convert.HappyPreferences
import happy.svg.convert.HappyTexture
import happy.svg.convert.toHappyPaint
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
import java.net.URLConnection
import java.util.UUID
import javax.imageio.ImageIO

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
    require(!isInteractive || innerCutout == 0f) { "Can't draw interactive ring" }
    require(bounds.w near bounds.h) { "Can't draw ellipse, only circles" }

    val circle = if (innerCutout == 0f) {
        path.utils.paths.circle(bounds.cx, bounds.cy, bounds.w / 2)
    } else {
        val rightScale = innerCutout / 100 * (1 - 0.015)
        ring(bounds.cx, bounds.cy, bounds.w / 2, rightScale * (bounds.w / 2))
    }

    possiblyInteractiveShape(circle, paint, isInteractive, isFixed, isSleeping, density, collision)
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
    val triangle = mutablePath().apply {
        moveTo(bounds.left, bounds.bottom)
        lineTo(bounds.cx, bounds.top)
        lineTo(bounds.right, bounds.bottom)
        close()
    }

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