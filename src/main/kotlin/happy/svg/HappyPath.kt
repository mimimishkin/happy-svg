package happy.svg

import happy.svg.HappyWheels.formatter
import path.utils.math.Vec2
import path.utils.math.distToSq
import path.utils.math.orZero
import path.utils.paths.*
import path.utils.paths.Command.Close
import path.utils.paths.Command.CubicTo
import path.utils.paths.Command.LineTo
import path.utils.paths.Command.MoveTo
import path.utils.paths.Command.QuadTo
import kotlin.collections.plusAssign
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class HappyPath(rawPath: Path, isAlreadyOptimized: Boolean = false) : HappyWheels.Format {
    val path = if (isAlreadyOptimized) rawPath else rawPath.minify().validate().simplify()
    val bounds = run {
        var minX = Double.MAX_VALUE
        var minY = Double.MAX_VALUE
        var maxX = Double.MIN_VALUE
        var maxY = Double.MIN_VALUE

        for (command in path) {
            val (x, y) = when (command) {
                is MoveTo -> command.p
                is CubicTo -> command.p
                is LineTo -> command.p
                is QuadTo -> command.p
                else -> continue
            }

            minX = min(minX, x)
            minY = min(minY, y)
            maxX = max(maxX, x)
            maxY = max(maxY, y)
        }

        Bounds(minX, minY, maxX - minX, maxY - minY)
    }

    companion object {
        private var currentId = 1
    }

    inner class HappyV(
        var point: Vec2,
        var leftAnchor: Vec2? = null,
        var rightAnchor: Vec2? = null,
    ) {
        override fun toString(): String {
            val needAnchors = (leftAnchor == null || leftAnchor!! near point) && (rightAnchor == null || rightAnchor!! near point)
            return if (needAnchors) {
                val (px, py) = point - bounds.center
                formatter.format(px) + "_" + formatter.format(py)
            } else {
                val (px, py) = point - bounds.center
                val (a1x, a1y) = leftAnchor?.let { it - point }.orZero()
                val (a2x, a2y) = rightAnchor?.let { it - point }.orZero()
                listOf(px, py, a1x, a1y, a2x, a2y).joinToString("_") { formatter.format(it) }
            }
        }
    }

    override val tag = "v"

    val id = currentId++

    var strokeOnly = false

    val nodes by lazy {
        val nodes = mutableListOf<HappyV>()
        var firstMove: Vec2? = null
        var lastMove: Vec2? = null
        var wasClosed = false

        fun makePoint(to: Vec2) {
            val last = nodes.lastOrNull()
            if (last == null) {
                nodes += HappyV(to)
            } else {
                if (abs(last.point distToSq to) > 0.005)
                    nodes += HappyV(to)
            }
        }

        fun close() {
            if (!wasClosed) {
                makePoint(lastMove!!)
                makePoint(firstMove!!)
            }
            wasClosed = true
        }

        val validPath = if (bounds.area > 20) {
            path
        } else {
            path.scale(20 / bounds.area, anchor = bounds.center)
        }

        for (command in validPath) {
            when (command) {
                is MoveTo -> {
                    if (firstMove == null) {
                        firstMove = command.p
                    } else {
                        close()
                        wasClosed = false
                    }

                    lastMove = command.p
                    makePoint(lastMove)
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

        close()
        nodes.last().run {
            if (point near firstMove!! && (leftAnchor ?: point) near point)
                nodes.removeLast()
        }
        nodes.first().run {
            if (point near nodes.last().point && (rightAnchor ?: point) near point)
                nodes.removeFirst()
        }

        nodes
    }

    override fun params(param: (String, Any) -> Unit) {
        param("f", !strokeOnly)
        param("id", id)
        param("n", nodes.size)
        nodes.forEachIndexed { i, node -> param("v$i",  node) }
    }
}