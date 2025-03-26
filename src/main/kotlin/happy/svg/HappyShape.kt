package happy.svg

import happy.svg.HappyWheels.Collision
import happy.svg.HappyWheels.ShapeType
import happy.svg.HappyWheels.ShapeType.Circle
import happy.svg.HappyWheels.ShapeType.Polygon
import happy.svg.HappyWheels.decimal
import path.utils.math.near
import path.utils.paths.Bounds
import java.awt.Color
import kotlin.math.roundToInt

// TODO: rearrange params like at the game
data class HappyShape(
    var type: ShapeType,
    var path: HappyPath?, // TODO: why do we need `val`?
    var bounds: Bounds,
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

    override val content: String
        get() = path?.format().orEmpty()

    override fun HappyWheels.Config.configure() {
        checkValid()

        type = this@HappyShape.type
        shapeBounds = bounds
        shapeRotation = rotation
        shapeColor = color.decimal
        shapeOutline = outline?.decimal
        shapeOpacity = (color.alpha / 255f * 100).roundToInt()
        if (type != Polygon) {
            shapeInteractive = isInteractive
        }
        shapeFixed = isFixed
        shapeSleeping = isSleeping
        shapeDensity = density
        shapeCollision = Collision.Everything
        if (type == Circle) {
            shapeInnerCutout = innerCutout
        }
    }

    fun checkValid() {
        if (path == null) {
            check(rotation == 0) { "Rotating a custom shape is not supported. Transform path instead" }
        }

        check(density in 0.1..100.0) { "Density must be between 0.1 and 100" }

        if (type == Circle) {
            check(bounds.w near bounds.h) { "Can't draw ellipse, only circles" }
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
    type = ShapeType.Rectangle,
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
    type = ShapeType.Triangle,
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

// TODO: 1. check for flat path 
// TODO: 2. check for max node count - 10
fun HappyPolygon(
    path: HappyPath,
    bounds: Bounds = path.bounds,
    color: Color,
    outline: Color? = null,
    isFixed: Boolean = false,
    isSleeping: Boolean = false,
    density: Float = 1f,
    collision: Collision = Collision.Everything,
) = HappyShape(
    type = Polygon,
    path = path,
    bounds = bounds,
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
    bounds: Bounds = path.bounds,
) = HappyShape(
    type = ShapeType.Art,
    path = path,
    bounds = bounds,
    color = color,
    outline = outline,
)