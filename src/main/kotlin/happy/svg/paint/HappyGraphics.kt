package happy.svg.paint

import path.utils.math.Transforms
import path.utils.paths.*
import java.awt.Paint
import java.awt.Shape
import java.awt.geom.AffineTransform

internal class HappyGraphics(
    val doFill: (part: Path, paint: HappyPaint) -> Unit
) : AbstractGraphics2D() {
    private var pathClip: Path? = null
    private var matrixTransform = Transforms.identical()
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
    override fun transform(Tx: AffineTransform) {
        super.transform(Tx)
        matrixTransform = _transform.toMatrixTransform()
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
            .let { pathClip?.and(it) ?: it }

        if (path.isNotEmpty()) {
            doFill(path, happyPaint ?: foreground.toHappyPaint(path.bounds))
        }
    }
}