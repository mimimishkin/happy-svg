package happy.svg

import happy.svg.HappyWheels.Collision
import happy.svg.HappyWheels.ShapeType.*
import path.utils.math.MatrixTransform
import path.utils.math.Transforms
import path.utils.math.Vec2
import path.utils.paths.Bounds
import path.utils.paths.Path
import path.utils.paths.intersect
import path.utils.paths.bounds
import path.utils.paths.rect
import java.awt.Color

@DslMarker
annotation class HappyLayerBuilderDsl

@HappyLayerBuilderDsl
interface HappyLayerBuilder {
    val color: Color
    val outline: Color?
    val isInteractive: Boolean
    val rotation: Int
    val isFixed: Boolean
    val isSleeping: Boolean
    val density: Float
    val collision: Collision
    val innerCutout: Float

    val transform: MatrixTransform
    val clip: Path?

    /**
     * Add shape to level, applying [transform] and [clip]
     *
     * Shapes of types [Rectangle], [Circle], [Triangle] will be converted to [Polygon] or [Art] when need
     */
    fun shape(shape: HappyShape)

    fun rectangle(
        bounds: Bounds,
        color: Color = this.color,
        outline: Color? = this.outline,
        rotation: Int = this.rotation,
        isInteractive: Boolean = this.isInteractive,
        isFixed: Boolean = this.isFixed,
        isSleeping: Boolean = this.isSleeping,
        density: Float = this.density,
        collision: Collision = this.collision
    ) = shape(HappyRectangle(
        bounds = bounds,
        color = color,
        outline = outline,
        rotation = rotation,
        isInteractive = isInteractive,
        isFixed = isFixed,
        isSleeping = isSleeping,
        density = density,
        collision = collision
    ))

    fun circle(
        bounds: Bounds,
        color: Color = this.color,
        outline: Color? = this.outline,
        rotation: Int = this.rotation,
        isInteractive: Boolean = this.isInteractive,
        isFixed: Boolean = this.isFixed,
        isSleeping: Boolean = this.isSleeping,
        density: Float = this.density,
        collision: Collision = this.collision,
        innerCutout: Float = this.innerCutout
    ) = shape(HappyCircle(
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
    ))

    fun triangle(
        bounds: Bounds,
        color: Color = this.color,
        outline: Color? = this.outline,
        rotation: Int = this.rotation,
        isInteractive: Boolean = this.isInteractive,
        isFixed: Boolean = this.isFixed,
        isSleeping: Boolean = this.isSleeping,
        density: Float = this.density,
        collision: Collision = this.collision
    ) = shape(HappyTriangle(
        bounds = bounds,
        color = color,
        outline = outline,
        rotation = rotation,
        isInteractive = isInteractive,
        isFixed = isFixed,
        isSleeping = isSleeping,
        density = density,
        collision = collision
    ))

    fun polygon(
        path: HappyPath,
        bounds: Bounds = path.bounds,
        color: Color = this.color,
        outline: Color? = this.outline,
        isFixed: Boolean = this.isFixed,
        isSleeping: Boolean = this.isSleeping,
        density: Float = this.density,
        collision: Collision = this.collision
    ) = shape(HappyPolygon(
        path = path,
        bounds = bounds,
        color = color,
        outline = outline,
        isFixed = isFixed,
        isSleeping = isSleeping,
        density = density,
        collision = collision
    ))

    fun art(
        path: Path,
        bounds: Bounds = path.bounds,
        color: Color = this.color,
        outline: Color? = this.outline,
    ) = shape(HappyArt(
        path = HappyPath(path),
        bounds = bounds,
        color = color,
        outline = outline,
    ))

    fun transform(
        color: Color = this.color,
        outline: Color? = this.outline,
        rotation: Int = this.rotation,
        isInteractive: Boolean = this.isInteractive,
        isFixed: Boolean = this.isFixed,
        isSleeping: Boolean = this.isSleeping,
        density: Float = this.density,
        collision: Collision = this.collision,
        innerCutout: Float = this.innerCutout,

        transform: MatrixTransform? = null,
        clip: Path? = null,
        block: HappyLayerBuilder.() -> Unit
    )

    fun rotate(
        theta: Double,
        center: Vec2 = Vec2(),
        block: HappyLayerBuilder.() -> Unit
    ) = transform(
        transform = Transforms.rotate(theta, center.x, center.y),
        block = block
    )

    fun translate(
        x: Double,
        y: Double,
        block: HappyLayerBuilder.() -> Unit
    ) = transform(
        transform = Transforms.translate(x, y),
        block = block
    )

    fun scale(
        x: Double,
        y: Double = x,
        block: HappyLayerBuilder.() -> Unit
    ) = transform(
        transform = Transforms.scale(x, y),
        block = block
    )

    fun clip(
        path: Path,
        block: HappyLayerBuilder.() -> Unit
    ) = transform(
        clip = path,
        block = block
    )

    fun clip(
        bounds: Bounds,
        block: HappyLayerBuilder.() -> Unit
    ) = transform(
        clip = rect(bounds),
        block = block
    )
}

internal class HappyLayerBuilderImpl(
    val destination: HappyLevel.Shapes,

    override val color: Color = Color(61, 136, 199),
    override val outline: Color? = null,
    override val isInteractive: Boolean = true,
    override val rotation: Int = 0,
    override val isFixed: Boolean = true,
    override val isSleeping: Boolean = false,
    override val density: Float = 1f,
    override val collision: Collision = Collision.Everything,
    override val innerCutout: Float = 0f,

    override val transform: MatrixTransform = Transforms.identical(),
    override val clip: Path? = null,
) : HappyLayerBuilder {

    override fun shape(shape: HappyShape) {
        // TODO: apply clip and transform
        destination.shapes += shape
    }

    override fun transform(
        color: Color,
        outline: Color?,
        rotation: Int,
        isInteractive: Boolean,
        isFixed: Boolean,
        isSleeping: Boolean,
        density: Float,
        collision: Collision,
        innerCutout: Float,
        transform: MatrixTransform?,
        clip: Path?,
        block: HappyLayerBuilder.() -> Unit
    ) {
        val fullTransform = if (transform != null) this.transform.post(transform) else this.transform
        // TODO: transform clip
        val fullClip = when {
            this.clip == null && clip == null -> null
            this.clip == null -> clip
            clip == null -> this.clip
            else -> this.clip intersect clip
        }

        val builder = HappyLayerBuilderImpl(
            destination = destination,

            color = color,
            outline = outline,
            rotation = rotation,
            isInteractive = isInteractive,
            isFixed = isFixed,
            isSleeping = isSleeping,
            density = density,
            collision = collision,
            innerCutout = innerCutout,

            transform = fullTransform,
            clip = fullClip,
        )

        builder.block()
    }
}