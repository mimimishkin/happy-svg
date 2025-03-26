package happy.svg

import happy.svg.HappyWheels.Collision
import happy.svg.HappyWheels.ShapeType
import path.utils.paths.MutablePath
import path.utils.paths.Path
import path.utils.paths.close
import path.utils.paths.lineTo
import path.utils.paths.moveTo
import path.utils.paths.mutablePath
import java.awt.Color

fun HappyLevel.Shapes.rectangle(
    color: Color,
    outline: Color? = null,
    isInteractive: Boolean = false,
    rotation: Int = 0,
    isFixed: Boolean = false,
    isSleeping: Boolean = false,
    density: Int = 1,
    collision: Collision = Collision.Everything
) {
    shapes += HappyRectangle(
        color = color,
        outline = outline,
        isInteractive = isInteractive,
        rotation = rotation,
        isFixed = isFixed,
        isSleeping = isSleeping,
        density = density,
        collision = collision
    )
}

fun HappyLevel.Shapes.circle(
    color: Color,
    outline: Color? = null,
    isInteractive: Boolean = false,
    rotation: Int = 0,
    isFixed: Boolean = false,
    isSleeping: Boolean = false,
    density: Int = 1,
    collision: Collision = Collision.Everything
) {
    shapes += HappyCircle(
        color = color,
        outline = outline,
        isInteractive = isInteractive,
        rotation = rotation,
        isFixed = isFixed,
        isSleeping = isSleeping,
        density = density,
        collision = collision
    )
}

fun HappyLevel.Shapes.triangle(
    color: Color,
    outline: Color? = null,
    interactive: Boolean = false,
    rotation: Int = 0,
    isFixed: Boolean = false,
    isSleeping: Boolean = false,
    density: Int = 1,
    collision: Collision = Collision.Everything
) {
    shapes += HappyTriangle(
        color = color,
        outline = outline,
        isInteractive = interactive,
        rotation = rotation,
        isFixed = isFixed,
        isSleeping = isSleeping,
        density = density,
        collision = collision
    )
}

fun HappyLevel.Shapes.polygon(
    path: HappyPath,
    color: Color,
    outline: Color? = null,
    isInteractive: Boolean = false,
    rotation: Int = 0,
    isFixed: Boolean = false,
    isSleeping: Boolean = false,
    density: Int = 1,
    collision: Collision = Collision.Everything
) {
    shapes += HappyPolygon(
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
}

fun HappyLevel.Shapes.art(
    path: Path,
    color: Color,
    outline: Color? = null,
    rotation: Int = 0
) {
    shapes += HappyArt(
        path = HappyPath(path),
        color = color,
        outline = outline,
        rotation = rotation,
    )
}

fun HappyLevel.Shapes.art(
    color: Color,
    outline: Color? = null,
    rotation: Int = 0,
    pathBuilder: MutablePath.() -> Path,
) = art(pathBuilder(mutablePath()), color, outline, rotation)

@DslMarker
annotation class HappyLevelBuilderDsl

@HappyLevelBuilderDsl
interface HappyLevelBuilder {
    fun info(block: HappyLevel.Info.() -> Unit)

    fun shapes(block: HappyLevel.Shapes.() -> Unit)
}

fun happyLevel(block: HappyLevelBuilder.() -> Unit): HappyLevel {
    val builder = object : HappyLevelBuilder {
        val info = HappyLevel.Info()
        val shapes = HappyLevel.Shapes()

        override fun info(block: HappyLevel.Info.() -> Unit) {
            info.block()
        }

        override fun shapes(block: HappyLevel.Shapes.() -> Unit) {
            shapes.block()
        }
    }

    builder.block()

    return HappyLevel(builder.info, builder.shapes)
}