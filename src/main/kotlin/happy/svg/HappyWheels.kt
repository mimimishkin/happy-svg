package happy.svg

import path.utils.paths.Bounds
import java.awt.Color
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

object HappyWheels  {
    interface Format {
        val tag: String

        fun params(param: (key: String, value: Any) -> Unit) {}

        fun children(child: (child: Format) -> Unit) {}

        fun format(level: Int = 0, builder: StringBuilder) {
            fun Any.toParamString(): String = when (this) {
                is String -> this
                is Float -> formatter.format(this)
                is Double -> formatter.format(this)
                is Boolean -> if (this) "t" else "f"
                is Color -> (red * 256 * 256 + green * 256 + blue).toString()
                else -> toString()
            }

            with(builder) {
                val space = "    ".repeat(level)
                append(space)
                append("<$tag")
                params { key, value ->
                    append(" $key=\"${value.toParamString()}\"")
                }

                val i = length
                children { child ->
                    child.format(level + 1, builder)
                    append("\n")
                }

                if (length > i) {
                    insert(i, ">\n")
                    append(space)
                    append("</$tag>")
                } else {
                    append("/>")
                }
            }
        }

        fun format() = StringBuilder().also { format(level = 0, builder = it) }.toString()
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

    const val MIN_VISIBLE_ALPHA = 2.55

    const val MIN_VISIBLE_AREA = 2.0

    internal val formatter = DecimalFormat("#.###", DecimalFormatSymbols(Locale.US))

    val LEVEL_BOUNDS = Bounds(0.0, 0.0, 20000.0, 10000.0)
}
