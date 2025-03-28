package happy.svg

import happy.svg.HappyWheels.scaled
import path.utils.math.Transforms
import path.utils.math.Vec2
import path.utils.math.orZero
import path.utils.paths.*
import path.utils.paths.Command.*

class HappyPath(path: Path, val bounds: Bounds = path.bounds) : HappyWheels.Format {
    companion object {
        private var currentId = 1
    }

    inner class HappyV(
        var point: Vec2,
        var leftAnchor: Vec2? = null,
        var rightAnchor: Vec2? = null,
    ) {
        fun format(index: Int): String {
            val needAnchors = (leftAnchor == null || leftAnchor!! near point) && (rightAnchor == null || rightAnchor!! near point)
            val value = if (needAnchors) {
                val (px, py) = point - bounds.center
                "${px.scaled}_${py.scaled}"
            } else {
                val (px, py) = point - bounds.center
                val (a1x, a1y) = leftAnchor?.let { it - point }.orZero()
                val (a2x, a2y) = rightAnchor?.let { it - point }.orZero()
                "${px.scaled}_${py.scaled}_${a1x.scaled}_${a1y.scaled}_${a2x.scaled}_${a2y.scaled}"
            }

            return "v$index=\"$value\""
        }
    }

    override val tag = "v"

    val id = currentId++

    var strokeOnly = false

    val nodes by lazy {
        val nodes = mutableListOf<HappyV>()
        var firstMove: Vec2? = null
        var move: Vec2? = null
        var wasClosed = false

        fun makePoint(to: Vec2) {
            val last = nodes.lastOrNull()
            if (last == null || !(last.point near to))
                nodes += HappyV(to)
        }
        fun close() {
            if (!wasClosed) {
                makePoint(move!!)

                if (firstMove != null)
                    makePoint(firstMove!!)
            }
            wasClosed = true
        }

        val validPath = if (bounds.area > 20) {
            path
        } else {
            path.transformWith(Transforms.scale(20 / bounds.area).apply {
                preTranslate(-bounds.cx, -bounds.cy)
                translate(bounds.cx, bounds.cy)
            })
        }

        validPath.minify().validate().simplify().iteratePath { command, _, _, moveTo ->
            move = moveTo

            when (command) {
                is MoveTo -> {
                    if (firstMove == null) {
                        firstMove = command.p

                        makePoint(move)
                    } else {
                        close()
                        wasClosed = false

                        makePoint(move)
                    }
                }

                is LineTo -> makePoint(command.p)

                is QuadTo -> {
                    val prev = nodes.last()
                    prev.rightAnchor = command.p1
                    makePoint(command.p)
                }

                is CubicTo -> {
                    val prev = nodes.last()
                    prev.rightAnchor = command.p1
                    makePoint(command.p)

                    val new = nodes.last()
                    new.leftAnchor = command.p2
                }

                is Close -> close()

                else -> throw AssertionError()
            }
        }

        // as a path may have several MoveTo commands, ensure a path is closed
        close()
        // remove the last point that matches to the first
        nodes.apply { removeLast() }
    }

    override fun HappyWheels.Config.configure() {
        strokeOnly = this@HappyPath.strokeOnly
        pathId = id
        nodesCount = nodes.size
        thisContent = nodes.withIndex().joinToString(separator = " ") { (i, v) -> v.format(i) }
    }
}