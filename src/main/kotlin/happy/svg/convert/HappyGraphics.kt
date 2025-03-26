package happy.svg.convert

import happy.svg.HappyLayerImpl
import happy.svg.HappyLevel
import happy.svg.art
import path.utils.paths.*
import java.awt.Shape

class HappyGraphics(destination: HappyLevel.Shapes, preferences: HappyPreferences) : AbstractGraphics2D() {
    private val layer = HappyLayerImpl(destination, preferences = preferences)

    private var wasClipped = false
    private var pathClip: Path? = null
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

    // TODO: text to text layers

    override fun fill(shape: Shape) {
        val path = shape
            .toPath()
            .transformWith(transform.toMatrixTransform())
            .let { pathClip?.intersect(it) ?: it }

        if (path.isNotEmpty()) {
            val happyPaint = foreground.toHappyPaint(path)

            layer.art(path, happyPaint)
        }
    }
}