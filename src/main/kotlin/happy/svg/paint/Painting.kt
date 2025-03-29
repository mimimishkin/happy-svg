package happy.svg.paint

import com.kitfox.svg.SVGDiagram
import com.kitfox.svg.SVGUniverse
import happy.svg.HappyLayer
import happy.svg.HappyWheels.Collision
import happy.svg.art
import happy.svg.convert.HappyPreferences
import happy.svg.isoscelesTriangle
import happy.svg.possiblyInteractiveShape
import happy.svg.transform
import happy.svg.truncRingSector
import path.utils.math.Transforms
import path.utils.math.Transforms.AspectRatio
import path.utils.math.near
import path.utils.paths.*
import java.awt.Color
import java.awt.TexturePaint
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.net.URI
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.PI

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
    paint: HappyPaint,
    isInteractive: Boolean = this.isInteractive,
    isFixed: Boolean = this.isFixed,
    isSleeping: Boolean = this.isSleeping,
    density: Float = this.density,
    collision: Collision = this.collision,
    ignoreLayer: Boolean = false,
) {
    paint.fill(path, preferences) { part, color ->
        possiblyInteractiveShape(part, color, null, isInteractive, isFixed, isSleeping, density, collision, ignoreLayer)
    }
}

fun HappyLayer.rectangle(
    bounds: Bounds,
    paint: HappyPaint,
    isInteractive: Boolean = this.isInteractive,
    isFixed: Boolean = this.isFixed,
    isSleeping: Boolean = this.isSleeping,
    density: Float = this.density,
    collision: Collision = this.collision,
    ignoreLayer: Boolean = false,
) = possiblyInteractiveShape(rect(bounds), paint, isInteractive, isFixed, isSleeping, density, collision, ignoreLayer)

fun HappyLayer.circle(
    bounds: Bounds,
    paint: HappyPaint,
    isInteractive: Boolean = this.isInteractive,
    isFixed: Boolean = this.isFixed,
    isSleeping: Boolean = this.isSleeping,
    density: Float = this.density,
    collision: Collision = this.collision,
    innerCutout: Float = this.innerCutout,
    ignoreLayer: Boolean = false,
) {
    require(bounds.w near bounds.h) { "Can't draw ellipse, only circles" }

    val radius = bounds.w / 2
    if (innerCutout == 0f) {
        possiblyInteractiveShape(
            path = circle(bounds.cx, bounds.cy, radius),
            paint = paint,
            isInteractive = isInteractive,
            isFixed = isFixed,
            isSleeping = isSleeping,
            density = density,
            collision = collision,
            ignoreLayer = ignoreLayer,
        )
    } else {
        val rightScale = innerCutout / 100 * (1 - 0.015)
        val outRadius = rightScale * radius

        if (!isInteractive) {
            art(ring(bounds.cx, bounds.cy, radius, outRadius), paint, ignoreLayer)
        } else {
            val sectorsCount = (2 * PI * radius / (preferences.minCurveLength - 0.5)).toInt() // add -0.5 to guarantee turning into lines
            val step = 2 * PI / sectorsCount
            for (i in 0 until sectorsCount) {
                val start = i * step
                val end = (i + 1) * step
                val sector = truncRingSector(bounds.cx, bounds.cy, radius, outRadius, start, end)

                polygon(sector, paint, isFixed, isSleeping, density, collision, ignoreLayer)
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
    collision: Collision = this.collision,
    ignoreLayer: Boolean = false,
) = possiblyInteractiveShape(isoscelesTriangle(bounds), paint, isInteractive, isFixed, isSleeping, density, collision, ignoreLayer)

fun HappyLayer.polygon(
    path: Path,
    paint: HappyPaint,
    isFixed: Boolean = this.isFixed,
    isSleeping: Boolean = this.isSleeping,
    density: Float = this.density,
    collision: Collision = this.collision,
    ignoreLayer: Boolean = false,
) {
    possiblyInteractiveShape(path, paint, true, isFixed, isSleeping, density, collision, ignoreLayer)
}

fun HappyLayer.art(
    path: Path,
    paint: HappyPaint,
    ignoreLayer: Boolean = false,
) {
    possiblyInteractiveShape(path, paint, false, ignoreLayer = ignoreLayer)
}

fun HappyLayer.picture(
    image: BufferedImage,
    viewport: Bounds? = null,
    aspectRatio: AspectRatio = AspectRatio.None,
    ignoreLayer: Boolean = false,
) {
    val anchor = image.raster.bounds.bounds2D
    val bounds = anchor.run { Bounds(x, y, width, height) }
    val paint = TexturePaint(image, anchor).toHappyPaint(bounds)
    layer(
        transform = viewport?.let { Transforms.rectToRect(bounds, it, aspectRatio) },
        reset = ignoreLayer
    ) {
        paint.fill(null, preferences) { part, color -> art(part, color, null) }
    }
}

fun HappyLayer.picture(
    image: SVGDiagram,
    viewport: Bounds? = null,
    aspectRatio: AspectRatio = AspectRatio.None,
    ignoreLayer: Boolean = false,
) {
    layer(
        outline = null,
        transform = viewport?.let {
            val svgViewport = image.viewRect.run { Bounds(x, y, width, height) }
            Transforms.rectToRect(svgViewport, it, aspectRatio)
        },
        reset = ignoreLayer
    ) {
        image.render(HappyGraphics { part, paint -> art(part, paint) })
    }
}

private val universe = SVGUniverse()

internal fun SVGDiagram(reader: Reader): SVGDiagram {
    val uri = universe.loadSVG(reader, UUID.randomUUID().toString())
    return universe.getDiagram(uri)
}

fun HappyLayer.picture(
    image: File,
    viewport: Bounds? = null,
    aspectRatio: AspectRatio = AspectRatio.None,
    ignoreLayer: Boolean = false,
    isSvg: Boolean
) {
    if (isSvg) {
        picture(SVGDiagram(image.reader()), viewport, aspectRatio, ignoreLayer)
    } else {
        picture(ImageIO.read(image), viewport, aspectRatio, ignoreLayer)
    }
}

fun HappyLayer.picture(
    image: InputStream,
    viewport: Bounds? = null,
    aspectRatio: AspectRatio = AspectRatio.None,
    ignoreLayer: Boolean = false,
    isSvg: Boolean
) {
    if (isSvg) {
        picture(SVGDiagram(image.bufferedReader()), viewport, aspectRatio, ignoreLayer)
    } else {
        picture(ImageIO.read(image.buffered()), viewport, aspectRatio, ignoreLayer)
    }
}

fun HappyLayer.picture(
    image: String,
    viewport: Bounds? = null,
    aspectRatio: AspectRatio = AspectRatio.None,
    ignoreLayer: Boolean = false,
    isSvg: Boolean
) {
    val file = File(image)
    if (file.exists()) {
        return picture(file, viewport, aspectRatio, ignoreLayer, isSvg)
    }

    val url = runCatching { URI.create(image).toURL() }
    if (url.isSuccess) {
        return picture(url.getOrThrow().openStream(), viewport, aspectRatio, ignoreLayer, isSvg)
    }

    throw IllegalArgumentException("Can't load image from $image")
}