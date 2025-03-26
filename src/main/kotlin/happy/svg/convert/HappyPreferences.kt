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
     * size to add to every gradient part to avoid spaces
     */
    var additionalGradientPartSize: Double,
    /**
     * rendering hints for vectorizing
     */
    var hints: RenderingHints,
    /**
     * size of pixel for vectorizing
     */
    var pixelSize: Double,
    /**
     * do vectorizing or not
     */
    var doVectorizing: Boolean
) {
    companion object {
        val default = HappyPreferences(
            minColorDifference = 0.05,
            minGradientPartSize = 2.5,
            colorCounts = 256,
            additionalGradientPartSize = 0.1,
            hints = RenderingHints(null),
            pixelSize = 5.0,
            doVectorizing = true
        )
    }
}