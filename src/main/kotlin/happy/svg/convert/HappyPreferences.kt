package happy.svg.convert

import java.awt.RenderingHints

data class HappyPreferences(
    /**
     * minimum difference between colors in gradient to split them
     */
    var minColorDifference: Double,
    /**
     * minimum size of gradient part to be rendered
     */
    var minGradientPartSize: Double,
    /**
     * number in range 2..256 to reduce colors in pictures
     */
    var colorCounts: Int,
    /**
     * size to add to every pixel's each side
     */
    var additionalPixelSize: Double,
    /**
     * do merge multiple pixels shapes with one color into one enormous shape
     */
    var mergePixels: Boolean,
    /**
     * size to add to every gradient part to avoid spaces
     */
    var additionalGradientPartSize: Double,
    /**
     * rendering hints for pixelating
     */
    var hints: RenderingHints,
    /**
     * size of pixel for pixelating
     */
    var pixelSize: Double,
    /**
     * curves with length less than this will turn into lines
     */
    var minCurveLength: Double,
) {
    companion object {
        val default = HappyPreferences(
            minColorDifference = 0.05,
            minGradientPartSize = 2.5,
            colorCounts = 32,
            additionalPixelSize = 0.075,
            mergePixels = true,
            additionalGradientPartSize = 0.1,
            hints = RenderingHints(null),
            pixelSize = 2.0,
            minCurveLength = 5.5,
        )
    }
}