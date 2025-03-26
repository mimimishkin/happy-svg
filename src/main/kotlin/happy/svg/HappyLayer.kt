package happy.svg

import happy.svg.HappyWheels.Collision
import happy.svg.HappyWheels.ShapeType.*
import happy.svg.convert.HappyPreferences
import path.utils.math.MatrixTransform
import path.utils.math.Transforms
import path.utils.math.Vec2
import path.utils.paths.*
import java.awt.Color

@DslMarker
annotation class HappyLayerDsl

@HappyLayerDsl
interface HappyLayer {
    val color: Color
    val outline: Color?
    val rotation: Int
    val isInteractive: Boolean
    val isFixed: Boolean
    val isSleeping: Boolean
    val density: Float
    val collision: Collision
    val innerCutout: Float

    val transform: MatrixTransform
    val clip: Path?
    val preferences: HappyPreferences
        get() = HappyPreferences.default

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
        path: Path,
        bounds: Bounds = path.bounds,
        color: Color = this.color,
        outline: Color? = this.outline,
        isFixed: Boolean = this.isFixed,
        isSleeping: Boolean = this.isSleeping,
        density: Float = this.density,
        collision: Collision = this.collision
    ) = shape(HappyPolygon(
        path = HappyPath(path),
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

    fun layer(
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
        preferences: HappyPreferences? = null,

        block: HappyLayer.() -> Unit
    )

    fun transform(
        transform: MatrixTransform? = null,
        block: HappyLayer.() -> Unit
    ) = layer(
        transform = transform,
        block = block
    )

    fun rotate(
        theta: Double,
        center: Vec2 = Vec2(),
        block: HappyLayer.() -> Unit
    ) = transform(
        transform = Transforms.rotate(theta, center.x, center.y),
        block = block
    )

    fun translate(
        x: Double,
        y: Double,
        block: HappyLayer.() -> Unit
    ) = transform(
        transform = Transforms.translate(x, y),
        block = block
    )

    fun scale(
        x: Double,
        y: Double = x,
        block: HappyLayer.() -> Unit
    ) = transform(
        transform = Transforms.scale(x, y),
        block = block
    )

    fun clip(
        path: Path,
        block: HappyLayer.() -> Unit
    ) = layer(
        clip = path,
        block = block
    )

    fun clip(
        bounds: Bounds,
        block: HappyLayer.() -> Unit
    ) = clip(
        path = rect(bounds),
        block = block
    )
}

internal class HappyLayerImpl(
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
    override val preferences: HappyPreferences
) : HappyLayer {

    override fun shape(shape: HappyShape) {
        // TODO: apply clip and transform
        destination.shapes += shape
    }

    override fun layer(
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
        preferences: HappyPreferences?,
        block: HappyLayer.() -> Unit
    ) {
        val fullTransform = if (transform != null) this.transform.post(transform) else this.transform
        // TODO: transform clip
        val fullClip = when {
            this.clip == null && clip == null -> null
            this.clip == null -> clip
            clip == null -> this.clip
            else -> this.clip intersect clip
        }

        val builder = HappyLayerImpl(
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
            preferences = preferences ?: this.preferences,
        )

        builder.block()
    }
}