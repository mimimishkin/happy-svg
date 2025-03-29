package happy.svg

import happy.svg.HappyWheels.Collision
import happy.svg.HappyWheels.Collision.Everything
import happy.svg.HappyWheels.ShapeType
import happy.svg.HappyWheels.ShapeType.*
import happy.svg.convert.HappyPreferences
import path.utils.math.MatrixTransform
import path.utils.math.Transforms
import path.utils.math.Vec2
import path.utils.math.near
import path.utils.paths.*
import java.awt.Color
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

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
    fun shape(
        type: ShapeType,
        path: Path?,
        bounds: Bounds?,
        color: Color  = this.color,
        outline: Color?  = this.outline,
        rotation: Int  = this.rotation,
        isInteractive: Boolean  = this.isInteractive,
        isFixed: Boolean  = this.isFixed,
        isSleeping: Boolean  = this.isSleeping,
        density: Float  = this.density,
        collision: Collision  = this.collision,
        innerCutout: Float = this.innerCutout,
        ignoreLayer: Boolean = false,
    )

    /**
     * @param reset resets [transform], [clip] and [preferences]
     */
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
        reset: Boolean = false,

        group: HappyGroup? = null,
        block: HappyLayer.() -> Unit
    )
}

fun HappyLayer.group(
    isSleeping: Boolean = false,
    isForeground: Boolean = false,
    opacity: Int = 100,
    isFixed: Boolean = false,
    isFixedAngle: Boolean = false,
    block: HappyLayer.() -> Unit
) = layer(
    group = HappyGroup(
        isSleeping = isSleeping,
        isForeground = isForeground,
        opacity = opacity,
        isFixed = isFixed,
        isFixedAngle = isFixedAngle,
    ),
    block = block,
)

fun HappyLayer.rectangle(
    bounds: Bounds,
    color: Color = this.color,
    outline: Color? = this.outline,
    rotation: Int = this.rotation,
    isInteractive: Boolean = this.isInteractive,
    isFixed: Boolean = this.isFixed,
    isSleeping: Boolean = this.isSleeping,
    density: Float = this.density,
    collision: Collision = this.collision,
    ignoreLayer: Boolean = false,
) = shape(
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
    collision = collision,
    innerCutout = 0f,
    ignoreLayer = ignoreLayer,
)

fun HappyLayer.circle(
    bounds: Bounds,
    color: Color = this.color,
    outline: Color? = this.outline,
    rotation: Int = this.rotation,
    isInteractive: Boolean = this.isInteractive,
    isFixed: Boolean = this.isFixed,
    isSleeping: Boolean = this.isSleeping,
    density: Float = this.density,
    collision: Collision = this.collision,
    innerCutout: Float = this.innerCutout,
    ignoreLayer: Boolean = false,
) = shape(
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
    innerCutout = innerCutout,
    ignoreLayer = ignoreLayer,
)

fun HappyLayer.triangle(
    bounds: Bounds,
    color: Color = this.color,
    outline: Color? = this.outline,
    rotation: Int = this.rotation,
    isInteractive: Boolean = this.isInteractive,
    isFixed: Boolean = this.isFixed,
    isSleeping: Boolean = this.isSleeping,
    density: Float = this.density,
    collision: Collision = this.collision,
    ignoreLayer: Boolean = false,
) = shape(
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
    collision = collision,
    innerCutout = 0f,
    ignoreLayer = ignoreLayer,
)

fun HappyLayer.polygon(
    path: Path,
    color: Color = this.color,
    outline: Color? = this.outline,
    isFixed: Boolean = this.isFixed,
    isSleeping: Boolean = this.isSleeping,
    density: Float = this.density,
    collision: Collision = this.collision,
    ignoreLayer: Boolean = false,
) = shape(
    type = Polygon,
    path = path,
    bounds = null,
    color = color,
    outline = outline,
    rotation = 0,
    isFixed = isFixed,
    isSleeping = isSleeping,
    density = density,
    collision = collision,
    innerCutout = 0f,
    ignoreLayer = ignoreLayer,
)

fun HappyLayer.art(
    path: Path,
    color: Color = this.color,
    outline: Color? = this.outline,
    ignoreLayer: Boolean = false,
) = shape(
    type = Art,
    path = path,
    bounds = null,
    color = color,
    outline = outline,
    rotation = 0,
    isFixed = false,
    isSleeping = false,
    density = 1f,
    collision = Everything,
    innerCutout = 0f,
    ignoreLayer = ignoreLayer,
)

/**
 * Arts can't be interactive, while this is the only possible state for polygons.
 *
 * This method uniforms their creation
 */
fun HappyLayer.possiblyInteractiveShape(
    path: Path,
    color: Color = this.color,
    outline: Color? = this.outline,
    isInteractive: Boolean = this.isInteractive,
    isFixed: Boolean = this.isFixed,
    isSleeping: Boolean = this.isSleeping,
    density: Float = this.density,
    collision: Collision = this.collision,
    ignoreLayer: Boolean = false,
) {
    if (isInteractive) {
        polygon(path, color, outline, isFixed, isSleeping, density, collision, ignoreLayer)
    } else {
        art(path, color, outline, ignoreLayer)
    }
}

fun HappyLayer.transform(
    transform: MatrixTransform? = null,
    block: HappyLayer.() -> Unit
) = layer(
    transform = transform,
    block = block
)

fun HappyLayer.rotate(
    theta: Double,
    center: Vec2 = Vec2(),
    block: HappyLayer.() -> Unit
) = transform(
    transform = Transforms.rotate(theta, center.x, center.y),
    block = block
)

fun HappyLayer.translate(
    x: Double,
    y: Double,
    block: HappyLayer.() -> Unit
) = transform(
    transform = Transforms.translate(x, y),
    block = block
)

fun HappyLayer.scale(
    x: Double,
    y: Double = x,
    block: HappyLayer.() -> Unit
) = transform(
    transform = Transforms.scale(x, y),
    block = block
)

fun HappyLayer.clip(
    path: Path,
    block: HappyLayer.() -> Unit
) = layer(
    clip = path,
    block = block
)

fun HappyLayer.clip(
    bounds: Bounds,
    block: HappyLayer.() -> Unit
) = clip(
    path = rect(bounds),
    block = block
)

// ------------------
// ------ IMPL ------
// ------------------

internal class HappyLayerImpl(
    val onShape: (HappyShape) -> Unit,
    val onGroup: (HappyGroup) -> Unit,

    override val color: Color = Color(61, 136, 199),
    override val outline: Color? = null,
    override val isInteractive: Boolean = true,
    override val rotation: Int = 0,
    override val isFixed: Boolean = true,
    override val isSleeping: Boolean = false,
    override val density: Float = 1f,
    override val collision: Collision = Everything,
    override val innerCutout: Float = 0f,

    override val transform: MatrixTransform = Transforms.identical(),
    override val clip: Path? = null,
    override val preferences: HappyPreferences
) : HappyLayer {
    val clipBounds by lazy { clip?.bounds }

    override fun shape(
        type: ShapeType,
        path: Path?,
        bounds: Bounds?,
        color: Color,
        outline: Color?,
        rotation: Int,
        isInteractive: Boolean,
        isFixed: Boolean,
        isSleeping: Boolean,
        density: Float,
        collision: Collision,
        innerCutout: Float,
        ignoreLayer: Boolean,
    ) {
        if (path != null && path.size <= 2) {
            return
        }

        if (ignoreLayer) {
            val path = if (type == Polygon) {
                val flatPath = requireNotNull(path) { "Path is null" }.toFlatPath(0.5)
                if (flatPath.isClockwise) flatPath.reversePath() else flatPath
            } else {
                path
            }

            val happyPath = path?.let { HappyPath(it) }
            val area = requireNotNull(happyPath?.bounds ?: bounds) { "Bounds are null" }.area
            if (area < HappyWheels.minVisibleArea) {
                return
            }

            val shape = HappyShape(
                type = type,
                path = happyPath,
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

            onShape(shape)
        } else {
            fun Path?.transformed(transform: MatrixTransform = this@HappyLayerImpl.transform) =
                requireNotNull(this) { "Path is null" }.transformWith(transform).let { clip?.and(it) ?: it }

            // assume a transform is affine
            fun MatrixTransform.isScaleAndTranslate(): Boolean =
                m01 near 0.0 && m10 near 0.0
            fun MatrixTransform.isRotateAndTranslate(): Boolean =
                sin(acos(m00)) near m10

            when (type) {
                Polygon -> polygon(
                    path = path.transformed(),
                    color = color,
                    outline = outline,
                    isFixed = isFixed,
                    isSleeping = isSleeping,
                    density = density,
                    collision = collision,
                    ignoreLayer = true
                )

                Art -> art(
                    path = path.transformed(),
                    color = color,
                    outline = outline,
                    ignoreLayer = true
                )

                else -> {
                    val bounds = requireNotNull(bounds) { "Bounds is null" }
                    val transform = if (rotation != 0) {
                        transform.rotate(Math.toDegrees(rotation.toDouble()), bounds.cx, bounds.cy)
                    } else {
                        transform
                    }

                    if (transform.isScaleAndTranslate()) {
                        val bounds = Bounds(
                            bounds.x + transform.m02,
                            bounds.y + transform.m12,
                            bounds.w * hypot(transform.m00, transform.m10),
                            bounds.h * hypot(transform.m01, transform.m11)
                        )

                        if (clipBounds?.overlap(bounds) == false) {
                            return
                        }

                        if (clip == null) {
                            return shape(
                                type = type,
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
                                ignoreLayer = true,
                            )
                        }
                    }

                    if (transform.isRotateAndTranslate()) {
                        val bounds = Bounds(
                            x = bounds.x + transform.m02,
                            y = bounds.y + transform.m12,
                            w = bounds.w,
                            h = bounds.h
                        )

                        if (clipBounds?.overlap(bounds) == false) {
                            return
                        }

                        if (clip == null) {
                            val rotation = transform.transform(bounds.center).angle(Vec2()).roundToInt()
                            return shape(
                                type = type,
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
                                ignoreLayer = true,
                            )
                        }
                    }

                    fun Path.draw() {
                        val path = this.transformed(transform)
                        if (path.size <= 2) {
                            return
                        }

                        possiblyInteractiveShape(
                            path = path,
                            color = color,
                            outline = outline,
                            isInteractive = isInteractive,
                            isFixed = isFixed,
                            isSleeping = isSleeping,
                            density = density,
                            collision = collision,
                            ignoreLayer = true
                        )
                    }

                    when (type) {
                        Rectangle -> rect(bounds).draw()

                        Circle -> {
                            require(bounds.w near bounds.h) { "Can't draw ellipse, only circles" }

                            val radius = bounds.w / 2
                            if (innerCutout == 0f) {
                                circle(bounds.cx, bounds.cy, radius).draw()
                            } else {
                                val rightScale = innerCutout / 100 * (1 - 0.015)
                                val outRadius = rightScale * radius

                                if (!isInteractive) {
                                    ring(bounds.cx, bounds.cy, radius, outRadius).draw()
                                } else {
                                    val count = (2 * PI * radius / (preferences.minCurveLength - 0.5)).toInt() // add -0.5 to guarantee turning into lines
                                    val step = 2 * PI / count
                                    for (i in 0 until count) {
                                        val start = i * step
                                        val end = (i + 1) * step
                                        val sector = truncRingSector(bounds.cx, bounds.cy, radius, outRadius, start, end)
                                        sector.draw()
                                    }
                                }
                            }
                        }

                        else -> isoscelesTriangle(bounds).draw()
                    }
                }
            }
        }
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
        reset: Boolean,
        group: HappyGroup?,
        block: HappyLayer.() -> Unit
    ) {
        if (reset) {
            return HappyLayerImpl(
                onShape = onShape,
                onGroup = onGroup,
                transform = transform ?: Transforms.identical(),
                clip = clip,
                preferences = preferences ?: this.preferences
            ).block()
        }

        val fullTransform = transform?.post(this.transform) ?: this.transform
        val fullClip = when {
            this.clip == null && clip == null -> null
            this.clip == null -> clip
            clip == null -> this.clip
            else -> this.clip and clip
        }

        val builder = HappyLayerImpl(
            onShape = if (group == null) onShape else { shape: HappyShape -> group.shapes += shape },
            onGroup = onGroup,

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
            clip = fullClip?.transformWith(fullTransform),
            preferences = preferences ?: this.preferences,
        )

        // TODO: do not create new layer if it can be emulated with this (i.e., when clip == null)

        builder.block()
        group?.let { onGroup(it) }
    }
}