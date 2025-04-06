package happy.svg

import happy.svg.HappyWheels.Collision
import happy.svg.HappyWheels.ShapeType
import happy.svg.HappyWheels.ShapeType.*
import path.utils.math.near
import path.utils.paths.Bounds
import java.awt.Color
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class HappyShape(
    var type: ShapeType,
    var path: HappyPath?,
    var bounds: Bounds?,
    var rotation: Int = 0,
    var color: Color,
    var outline: Color? = null,
    var isInteractive: Boolean = false,
    var isFixed: Boolean = false,
    var isSleeping: Boolean = false,
    var density: Float = 1f,
    var collision: Collision = Collision.Everything,
    var innerCutout: Float = 0f,
) : HappyWheels.Format {
    override val tag = "sh"

    override fun params(param: (String, Any) -> Unit) {
        checkValid()

        param("t", type.number)
        if (type != Polygon)
            param("i", isInteractive)

        val bounds = when (type) {
            Art, Polygon -> path!!.bounds
            Triangle -> bounds!!.copy().apply {
                // happy wheels render triangles with this offset ¯\(0_o)/¯
                val r = Math.toRadians(rotation.toDouble())
                y += (1.0 / 6.0) * h * cos(r)
                x -= (1.0 / 6.0) * h * sin(r)
            }
            else -> bounds!!
        }
        param("p0", bounds.cx)
        param("p1", bounds.cy)
        param("p2", bounds.w)
        param("p3", bounds.h)
        param("p4", rotation)
        param("p5", isFixed)
        param("p6", isSleeping)
        param("p7", density)
        param("p8", color)
        param("p9", (outline ?: -1))
        param("p10", (color.alpha / 255f * 100).roundToInt())
        param("p11", collision.number)

        if (type == Circle)
            param("p12", innerCutout)
    }

    override fun children(child: (HappyWheels.Format) -> Unit) {
        path?.let(child)
    }

    fun checkValid() {
        if (type == Polygon || type == Art) {
            check(path != null) { "Path is null" }
            check(bounds == null) { "Bounds for custom shape are not supported. Transform path instead" }
            check(rotation == 0) { "Rotating a custom shape is not supported. Transform path instead" }
        } else {
            check(bounds != null) { "Bounds are null" }
        }

        check(density in 0.0..100.0) { "Density must be between 0.1 and 100" }

        if (type == Circle) {
            check(bounds!!.w near bounds!!.h) { "Can't draw ellipse, only circles" }
            check(innerCutout in 0.0..100.0) { "Inner cutout must be between 0.1 and 100" }
        }
    }
}

fun HappyRectangle(
    bounds: Bounds,
    color: Color,
    outline: Color? = null,
    rotation: Int = 0,
    isInteractive: Boolean = false,
    isFixed: Boolean = false,
    isSleeping: Boolean = false,
    density: Float = 1f,
    collision: Collision = Collision.Everything
) = HappyShape(
    type = Rectangle,
    path = null,
    bounds = bounds,
    color = color,
    outline = outline,
    rotation = rotation,
    isInteractive = isInteractive,
    isFixed = isFixed,
    isSleeping = isSleeping,
    density = density,
    collision = collision
)

fun HappyCircle(
    bounds: Bounds,
    color: Color,
    outline: Color? = null,
    rotation: Int = 0,
    isInteractive: Boolean = false,
    isFixed: Boolean = false,
    isSleeping: Boolean = false,
    density: Float = 1f,
    collision: Collision = Collision.Everything,
    innerCutout: Float = 0f
) = HappyShape(
    type = Circle,
    path = null,
    bounds = bounds,
    color = color,
    outline = outline,
    rotation = rotation,
    isInteractive = isInteractive,
    isFixed = isFixed,
    isSleeping = isSleeping,
    density = density,
    collision = collision,
    innerCutout = innerCutout
)

fun HappyTriangle(
    bounds: Bounds,
    color: Color,
    outline: Color? = null,
    rotation: Int = 0,
    isInteractive: Boolean = false,
    isFixed: Boolean = false,
    isSleeping: Boolean = false,
    density: Float = 1f,
    collision: Collision = Collision.Everything
) = HappyShape(
    type = Triangle,
    path = null,
    bounds = bounds,
    color = color,
    outline = outline,
    rotation = rotation,
    isInteractive = isInteractive,
    isFixed = isFixed,
    isSleeping = isSleeping,
    density = density,
    collision = collision
)

fun HappyPolygon(
    path: HappyPath,
    color: Color,
    outline: Color? = null,
    isFixed: Boolean = false,
    isSleeping: Boolean = false,
    density: Float = 1f,
    collision: Collision = Collision.Everything,
) = HappyShape(
    type = Polygon,
    path = path,
    bounds = null,
    color = color,
    outline = outline,
    isFixed = isFixed,
    isSleeping = isSleeping,
    density = density,
    collision = collision
)

fun HappyArt(
    path: HappyPath,
    color: Color,
    outline: Color? = null,
) = HappyShape(
    type = Art,
    path = path,
    bounds = null,
    color = color,
    outline = outline,
)