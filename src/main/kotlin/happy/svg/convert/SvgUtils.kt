package happy.svg.convert

import com.kitfox.svg.SVGDiagram
import com.kitfox.svg.SVGUniverse
import happy.svg.convert.SadGraphics.Companion.svgUniverse
import java.io.Reader
import java.util.UUID

private val universe = SVGUniverse()

fun SVGDiagram(reader: Reader): SVGDiagram {
    val uri = svgUniverse.loadSVG(reader, UUID.randomUUID().toString())
    return svgUniverse.getDiagram(uri)
}