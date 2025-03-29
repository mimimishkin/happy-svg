package happy.svg

import path.utils.math.Vec2
import path.utils.paths.Bounds
import java.awt.Color
import java.util.Locale
import kotlin.ranges.contains

object HappyWheels  {
    interface Format {
        val tag: String

        fun Config.configure()

        fun format(): String = with(Config().apply { configure() }) {
            buildString {
                append("<$tag ")
                for ((key, value) in namedParams) {
                    append("$key=\"$value\" ")
                }
                deleteAt(lastIndex)

                if (thisContent.isNotEmpty()) {
                    append(thisContent)
                }

                if (children.isNotEmpty()) {
                    append(">\n")
                    for (child in children) {
                        val child = child.format().replace("\n", "\n    ")
                        append("    $child\n")
                    }
                    append("</$tag>")
                } else {
                    append("/>")
                }
            }
        }
    }

    enum class Character(val number: Int) {
        WheelchairGuy(1),
        SegwayGuy(2),
        IrresponsibleDad(3),
        EffectiveShopper(4),
        MopedCouple(5),
        LawnmowerMan(6),
        ExplorerGuy(7),
        SantaClaus(8),
        PogostickMan(9),
        IrresponsibleMom(10),
        HelicopterMan(11),
    }

    enum class Background(val number: Int) {
        Color(0), GreenHills(1), City(2)
    }

    /**
     * This value determines what the shape will collide with.
     */
    enum class Collision(val number: Int) {
        /**
         * Collides with everything.
         */
        Everything(1),

        /**
         * Collides with mostIy everything but characters.
         */
        NotCharacter(2),

        /**
         * Collides with nothing. (mostly for use with joints. If you'd Ike to
         * just use shapes as art, uncheck interactive instead)
         */
        Nothing(3),

        /**
         * Collides with everything except other shapes with collision set to
         * this value. (useful for something like a pair of attached legs that
         * уои don't want to collide with each other)
         */
        NotThis(4),


        /**
         * Collides only with fixed shapes.
         */
        Fixed(5),

        /**
         * Collides only with fixed shapes and other shapes with a collision set to this
         * value.
         */
        FixedAndThis(6),

        /**
         * Collides only with characters.
         */
        Character(7)
    }

    enum class ShapeType(val number: Int) {
        Rectangle(0), Circle(1), Triangle(2), Polygon(3), Art(4)
    }

    class Config {
        var type: ShapeType? = null
        var shapeInteractive: Boolean? = null
        var shapeBounds: Bounds? = null
        var shapeRotation: Int? = null
        var shapeFixed: Boolean? = null
        var shapeSleeping: Boolean? = null
        var shapeDensity: Float? = null
        var shapeColor: Int? = null
        var shapeOutline: Int? = null
        var shapeOpacity: Int? = null
        var shapeCollision: Collision? = null
        var shapeInnerCutout: Float? = null
        var groupCenter: Vec2? = null
        var groupRotation: Int? = null
        var groupCenterOffset: Vec2? = null
        var groupIsForeground: Boolean? = null
        var groupOpacity: Int? = null
        var groupSleeping: Boolean? = null
        var groupIsFixed: Boolean? = null
        var groupIsFixedAngle: Boolean? = null
        var version: String? = null
        var characterPosition: Vec2? = null
        var character: Character? = null
        var forceCharacter: Boolean? = null
        var hideVehicle: Boolean? = null
        var backgroundType: Background? = null
        var backgroundColor: Int? = null
        var strokeOnly: Boolean? = null
        var pathId: Int? = null
        var nodesCount: Int? = null
        val unknown: MutableMap<String, String> = mutableMapOf()

        var thisContent: String = ""
        var children: List<Format> = emptyList()

        val namedParams: Map<String, String> get() {
            val params = mutableMapOf<String, String>()

            if (type != null) {
                params += "t" to type!!.number.toString()
            }
            if (shapeInteractive != null) {
                params += "i" to shapeInteractive!!.symbol
            }
            if (shapeBounds != null) {
                params += "p0" to shapeBounds!!.cx.scaled.toString()
                params += "p1" to shapeBounds!!.cy.scaled.toString()
                params += "p2" to shapeBounds!!.w.scaled.toString()
                params += "p3" to shapeBounds!!.h.scaled.toString()
            }
            if (shapeRotation != null) {
                params += "p4" to shapeRotation!!.toString()
            }
            if (shapeFixed != null) {
                params += "p5" to shapeFixed!!.symbol
            }
            if (shapeSleeping != null) {
                params += "p6" to shapeSleeping!!.symbol
            }
            if (shapeDensity != null) {
                check(shapeDensity!! in 0.0..100.0) { "Opacity must be between 0.1 and 100" }
                params += "p7" to shapeDensity!!.scaled
            }
            if (shapeColor != null) {
                params += "p8" to shapeColor!!.toString()
            }
            if (shapeOutline != null) {
                params += "p9" to shapeOutline!!.toString()
            }
            if (shapeOpacity != null) {
                check(shapeOpacity!! in 0..100) { "Opacity must be between 0.1 and 100" }
                params += "p10" to shapeOpacity!!.toString()
            }
            if (shapeCollision != null) {
                params += "p11" to shapeCollision!!.number.toString()
            }
            if (shapeInnerCutout != null) {
                check(shapeInnerCutout!! in 0.0..100.0) { "Inner cutout must be between 0.1 and 100" }
                params += "p12" to shapeInnerCutout!!.toString()
            }
            if (groupCenter != null) {
                params += "x" to groupCenter!!.x.scaled
                params += "y" to groupCenter!!.y.scaled
            }
            if (groupRotation != null) {
                params += "r" to groupRotation!!.toString()
            }
            if (groupCenterOffset != null) {
                params += "ox" to groupCenterOffset!!.x.scaled
                params += "oy" to groupCenterOffset!!.y.scaled
            }
            if (groupIsForeground != null) {
                params += "f" to groupIsForeground!!.toString()
            }
            if (groupOpacity != null) {
                check(groupOpacity!! in 0..100) { "Opacity must be between 0.1 and 100" }
                params += "o" to groupOpacity!!.toString()
            }
            if (groupSleeping != null) {
                params += "s" to groupSleeping!!.symbol
            }
            if (groupIsFixed != null) {
                params += "im" to groupIsFixed!!.symbol
            }
            if (groupIsFixedAngle != null) {
                params += "fr" to  groupIsFixedAngle!!.symbol
            }
            if (version != null) {
                params += "v" to version!!
            }
            if (characterPosition != null) {
                params += "x" to characterPosition!!.x.scaled
                params += "y" to characterPosition!!.y.scaled
            }
            if (character != null) {
                params += "c" to character!!.number.toString()
            }
            if (forceCharacter != null) {
                params += "f" to forceCharacter!!.symbol
            }
            if (hideVehicle != null) {
                params += "h" to hideVehicle!!.symbol
            }
            if (backgroundType != null) {
                params += "bg" to backgroundType!!.number.toString()
            }
            if (backgroundColor != null) {
                params += "bgc" to backgroundColor!!.toString()
            }
            if (strokeOnly != null) {
                params += "f" to (!strokeOnly!!).symbol
            }
            if (pathId != null) {
                params += "id" to pathId!!.toString()
            }
            if (nodesCount != null) {
                params += "n" to nodesCount!!.toString()
            }
            params += unknown

            return params
        }
    }

    // TODO: optimize render removing shapes lower than this
    const val minVisibleAlpha = 2.55

    const val minVisibleArea = 2.0

    val Float.scaled get() = String.format(Locale.US, "%.3f", this)
    val Double.scaled get() = String.format(Locale.US, "%.3f", this)

    val Boolean.symbol get() = if (this) "t" else "f"

    val Color.decimal get() = red * 256 * 256 + green * 256 + blue

    val levelBounds = Bounds(0.0, 0.0, 20000.0, 10000.0)
}
