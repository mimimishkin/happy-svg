package happy.svg.convert

import jankovicsandras.imagetracer.ImageTracer
import path.utils.paths.Path
import path.utils.paths.close
import path.utils.paths.lineTo
import path.utils.paths.moveTo
import path.utils.paths.mutablePath
import path.utils.paths.quadTo
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

object Vectorizing {
    inline fun doVectorize(
        img: BufferedImage,
        translateX: Double,
        translateY: Double,
        pixSize: Double,
        processPart: (path: Path, color: Color) -> Unit
    ) {
        val imageData = ImageTracer.loadImageData(img)

        // TODO: preferences
        val options = hashMapOf(
            "pathomit" to 0.5f,
            "roundcoords" to 5f,
            "colorsampling" to 1f,
            "desc" to 0f,
            "scale" to pixSize.toFloat(),
        ).let { ImageTracer.checkoptions(it) }

        val traceData = ImageTracer.imagedataToTracedata(imageData, options, null)
        traceData.layers.forEachIndexed { index, layer ->
            val c = traceData.palette[index]
            val color = Color(c[0] + 128, c[1] + 128, c[2] + 128, c[3] + 128)

            layer.forEach { pathData ->
                val path =
                    mutablePath().moveTo(pathData[0][1] * pixSize + translateX, pathData[0][2] * pixSize + translateY)

                for (segment in pathData) {
                    if (segment[0] == 1.0) {
                        path.lineTo(
                            segment[3] * pixSize + translateX,
                            segment[4] * pixSize + translateY
                        )
                    } else {
                        path.quadTo(
                            segment[3] * pixSize + translateX,
                            segment[4] * pixSize + translateY,
                            segment[5] * pixSize + translateX,
                            segment[6] * pixSize + translateY
                        )
                    }
                }
                path.close()

                processPart(path, color)
            }
        }
    }

    @PublishedApi
    internal inline fun doPixilateSimple(
        img: BufferedImage,
        processPixel: (x: Int, y: Int, color: Int) -> Unit
    ) {
        val pixels = img.raster.getPixels(0, 0, img.width, img.height, null as IntArray?)

        for (y in 0 until img.height) {
            for (x in 0 until img.width) {
                processPixel(x, y, pixels[x + y * img.width])
            }
        }
    }

    inline fun doPixilate(
        img: BufferedImage,
        translateX: Double,
        translateY: Double,
        scaleX: Double,
        scaleY: Double,
        colorCount: Int = 256,
        processPixel: (x: Double, y: Double, w: Double, h: Double, color: Color) -> Unit
    ) {
        doPixilateSimple(img) { x, y, color ->
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            val a = (color shr 24) and 0xFF

            val reducedColor = Color(
                ((r / 255f * colorCount).roundToInt() / colorCount.toFloat() * 255).roundToInt(),
                ((g / 255f * colorCount).roundToInt() / colorCount.toFloat() * 255).roundToInt(),
                ((b / 255f * colorCount).roundToInt() / colorCount.toFloat() * 255).roundToInt(),
                a,
            )

            processPixel(x + translateX, y + translateY, scaleX, scaleY, reducedColor)
        }
    }
}
