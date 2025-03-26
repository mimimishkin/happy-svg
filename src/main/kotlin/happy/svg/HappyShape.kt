package happy.svg

import happy.svg.HappyWheels.Collision
import happy.svg.HappyWheels.ShapeType
import happy.svg.HappyWheels.decimal
import java.awt.Color
import kotlin.math.roundToInt

data class HappyShape(
    var type: ShapeType,
    val path: HappyPath?, // TODO: do we need `val`?
    var color: Color,
    var outline: Color? = null,
    var isInteractive: Boolean = false,
    var rotation: Int = 0, // TODO: possibly delete when we have a `path`
    var isFixed: Boolean = false,
    var isSleeping: Boolean = false,
    var density: Int = 1,
    var collision: Collision = Collision.Everything

    // TODO: x, y, width, height, scale factor, ring
) : HappyWheels.Format {
    override val tag = "sh"

    override val content: String
        get() = path?.format().orEmpty()

    override fun HappyWheels.Config.configure() {
        type = this@HappyShape.type
        shapeInteractive = isInteractive
        if (path != null) {
            shapeBounds = path.bounds
        }
        shapeRotation = rotation
        shapeFixed = isFixed
        shapeSleeping = isSleeping
        shapeDensity = density
        shapeColor = color.decimal
        shapeOutline = outline?.decimal
        shapeOpacity = (color.alpha / 255f * 100).roundToInt()
        shapeCollision = Collision.Everything
    }

    fun checkValid() {
        // TODO:
    }
}

fun HappyRectangle(
    color: Color,
    outline: Color? = null,
    isInteractive: Boolean = false,
    rotation: Int = 0,
    isFixed: Boolean = false,
    isSleeping: Boolean = false,
    density: Int = 1,
    collision: Collision = Collision.Everything
) = HappyShape(
    type = ShapeType.Rectangle,
    path = null,
    color = color,
    outline = outline,
    isInteractive = isInteractive,
    rotation = rotation,
    isFixed = isFixed,
    isSleeping = isSleeping,
    density = density,
    collision = collision
)

fun HappyCircle(
    color: Color,
    outline: Color? = null,
    isInteractive: Boolean = false,
    rotation: Int = 0,
    isFixed: Boolean = false,
    isSleeping: Boolean = false,
    density: Int = 1,
    collision: Collision = Collision.Everything
) = HappyShape(
    type = ShapeType.Circle,
    path = null,
    color = color,
    outline = outline,
    isInteractive = isInteractive,
    rotation = rotation,
    isFixed = isFixed,
    isSleeping = isSleeping,
    density = density,
    collision = collision
)

fun HappyTriangle(
    color: Color,
    outline: Color? = null,
    isInteractive: Boolean = false,
    rotation: Int = 0,
    isFixed: Boolean = false,
    isSleeping: Boolean = false,
    density: Int = 1,
    collision: Collision = Collision.Everything
) = HappyShape(
    type = ShapeType.Triangle,
    path = null,
    color = color,
    outline = outline,
    isInteractive = isInteractive,
    rotation = rotation,
    isFixed = isFixed,
    isSleeping = isSleeping,
    density = density,
    collision = collision
)

fun HappyPolygon(
    path: HappyPath,
    color: Color,
    outline: Color? = null,
    isInteractive: Boolean = false,
    rotation: Int = 0,
    isFixed: Boolean = false,
    isSleeping: Boolean = false,
    density: Int = 1,
    collision: Collision = Collision.Everything
) = HappyShape(
    type = ShapeType.Polygon,
    path = path,
    color = color,
    outline = outline,
    isInteractive = isInteractive,
    rotation = rotation,
    isFixed = isFixed,
    isSleeping = isSleeping,
    density = density,
    collision = collision
)

fun HappyArt(
    path: HappyPath,
    color: Color,
    outline: Color? = null,
    rotation: Int
) = HappyShape(
    type = ShapeType.Art,
    path = path,
    color = color,
    outline = outline,
    rotation = rotation,
)