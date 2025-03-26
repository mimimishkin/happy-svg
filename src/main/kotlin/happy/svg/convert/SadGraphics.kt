package happy.svg.convert

import happy.svg.HappyArt
import happy.svg.HappyLevel
import happy.svg.HappyPath
import path.utils.paths.*
import java.awt.Shape

class SadGraphics(val prefs: HappyPreferences) : AbstractGraphics2D() {
    private val _layers = mutableListOf<HappyLevel.Shapes>()
    val layers: List<HappyLevel.Shapes> = _layers

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
            val layer = HappyLevel.Shapes()
            foreground.toHappyPaint(path).doFill(prefs) { fill, color ->
                val fill = fill intersect path
                layer.shapes += HappyArt(HappyPath(fill), color)
            }
            _layers += layer
        }
    }
}