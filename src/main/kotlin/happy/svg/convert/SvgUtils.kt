package happy.svg.convert

import com.kitfox.svg.SVGDiagram
import com.kitfox.svg.SVGUniverse
import java.io.Reader
import java.util.UUID

private val universe = SVGUniverse()

fun SVGDiagram(reader: Reader): SVGDiagram {
    val uri = universe.loadSVG(reader, UUID.randomUUID().toString())
    return universe.getDiagram(uri)
}