package happy.svg

import happy.svg.convert.AbstractGraphics2D
import happy.svg.convert.HappyPaint
import happy.svg.convert.HappyPreferences
import happy.svg.convert.toHappyPaint
import path.utils.math.MatrixTransform
import path.utils.paths.Path
import path.utils.paths.bounds
import path.utils.paths.intersect
import path.utils.paths.toMatrixTransform
import path.utils.paths.toPath
import path.utils.paths.transformWith
import java.awt.Paint
import java.awt.Shape
import java.awt.geom.AffineTransform

internal class HappyGraphics(destination: HappyLevel.Shapes, preferences: HappyPreferences) : AbstractGraphics2D() {
    private val layer = HappyLayerImpl(destination, preferences = preferences)
    private var pathClip: Path? = null
    private var matrixTransform = MatrixTransform()
    private var happyPaint: HappyPaint? = null

    private var wasClipped = false
    override var _clip: Shape?
        get() = super._clip
        set(value) {
            super._clip = value

            if (!wasClipped) {
                wasClipped = true
                clipRect(0, 0, 20000, 10000)
            } else {
                pathClip = _clip?.toPath()
            }
        }
    override fun setClip(clip: Shape?) {
        wasClipped = false
        super.setClip(clip)
    }

    override var _transform: AffineTransform
        get() = super._transform
        set(value) {
            super._transform = value

            matrixTransform = value.toMatrixTransform()
        }

    override var foreground: Paint
        get() = super.foreground
        set(value) {
            super.foreground = value
            happyPaint = value.toHappyPaint()
        }

    // TODO: text to text layers

    override fun fill(shape: Shape) {
        val path = shape
            .toPath()
            .transformWith(matrixTransform)
            .let { pathClip?.intersect(it) ?: it }

        if (path.isNotEmpty()) {
            layer.art(path, happyPaint ?: foreground.toHappyPaint(path.bounds))
        }
    }
}