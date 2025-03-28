package happy.svg

import path.utils.paths.*
import kotlin.math.cos
import kotlin.math.sin

fun isoscelesTriangle(bounds: Bounds) = mutablePath()
    .moveTo(bounds.left, bounds.bottom)
    .lineTo(bounds.cx, bounds.top)
    .lineTo(bounds.right, bounds.bottom)
    .close()

fun circleSector(cx: Double, cy: Double, r: Double, start: Double, end: Double) = mutablePath()
    .moveTo(cx, cy)
    .lineTo(cx + r * cos(start), cy + r * sin(start))
    .arcTo(r, r, 0.0, false, true, cx + r * cos(end), cy + r * sin(end))
    .close()

fun truncCircleSector(cx: Double, cy: Double, r: Double, start: Double, end: Double) = mutablePath()
    .moveTo(cx, cy)
    .lineTo(cx + r * cos(start), cy + r * sin(start))
    .lineTo(cx + r * cos(end), cy + r * sin(end))
    .close()

fun ringSector(cx: Double, cy: Double, outRadius: Double, innerRadius: Double, start: Double, end: Double) = mutablePath()
    .moveTo(cx + outRadius * cos(start), cy + outRadius * sin(start))
    .arcTo(outRadius, outRadius, 0.0, false, true, cx + outRadius * cos(end), cy + outRadius * sin(end))
    .lineTo(cx + innerRadius * cos(end), cy + innerRadius * sin(end))
    .arcTo(innerRadius, innerRadius, 0.0, false, false, cx + innerRadius * cos(start), cy + innerRadius * sin(start))
    .close()

fun truncRingSector(cx: Double, cy: Double, outRadius: Double, innerRadius: Double, start: Double, end: Double) = mutablePath()
    .moveTo(cx + outRadius * cos(start), cy + outRadius * sin(start))
    .lineTo(cx + outRadius * cos(end), cy + outRadius * sin(end))
    .lineTo(cx + innerRadius * cos(end), cy + innerRadius * sin(end))
    .lineTo(cx + innerRadius * cos(start), cy + innerRadius * sin(start))
    .close()