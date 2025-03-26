package happy.svg

import path.utils.math.Vec2
import path.utils.paths.Bounds
import java.awt.Color
import java.io.File

object HappyWheels  {
    interface Format {
        val tag: String

        val content: String
            get() = ""

        fun Config.configure() = Unit

        fun format(): String {
            val (contents, params) = Config().apply { configure() }.let { it.childContents to it.namedParams }

            val inlineContent = contents.isEmpty() && !content.contains('<')
            val builder = StringBuilder()
            return if (inlineContent) {
                builder.append("<$tag ")
                params.forEach { (key, value) -> builder.append("$key=\"$value\" ") }
                builder.deleteAt(builder.lastIndex)
                builder.append(content)
                builder.append("/>")
                builder.toString()
            } else {
                builder.append("<$tag> ")
                params.forEach { (key, value) -> builder.append("$key=\"$value\" ") }
                builder.append("\b\n")
                contents.forEach { builder.append("    $it\n") }
                builder.append("</$tag>")
                builder.toString()
            }
        }
    }

    // TODO: remove icon from here
    enum class Character(val number: Int, val icon: File) {
        WheelchairGuy(1, File("src/jvmMain/resources/characters/WheelchairGuy.svg")),
        SegwayGuy(2, File("src/jvmMain/resources/characters/SegwayGuy.svg")),
        IrresponsibleDad(3, File("src/jvmMain/resources/characters/IrresponsibleDad.svg")),
        EffectiveShopper(4, File("src/jvmMain/resources/characters/EffectiveShopper.svg")),
        MopedCouple(5, File("src/jvmMain/resources/characters/MopedCouple.svg")),
        LawnmowerMan(6, File("src/jvmMain/resources/characters/LawnmowerMan.svg")),
        ExplorerGuy(7, File("src/jvmMain/resources/characters/ExplorerGuy.svg")),
        SantaClaus(8, File("src/jvmMain/resources/characters/SantaClaus.svg")),
        PogostickMan(9, File("src/jvmMain/resources/characters/PogostickMan.svg")),
        IrresponsibleMom(10, File("src/jvmMain/resources/characters/IrresponsibleMom.svg")),
        HelicopterMan(11, File("src/jvmMain/resources/characters/HelicopterMan.svg")),
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

        var childContents: List<String> = emptyList()

        val namedParams: Map<String, String> get() {
            val params = mutableMapOf<String, String>()

            if (type != null) {
                params += "t" to type!!.number.toString()
            }
            if (shapeInteractive != null) {
                params += "i" to if (shapeInteractive!!) "t" else "f"
            }
            if (shapeBounds != null) {
                params += "p0" to shapeBounds!!.cx.scaled.toString()
                params += "p1" to shapeBounds!!.cy.scaled.toString()
                params += "p2" to shapeBounds!!.w.scaled.toString()
                params += "p3" to shapeBounds!!.h.scaled.toString()
            }
            if (shapeRotation != null) {
                params += "p4" to shapeRotation.toString()
            }
            if (shapeFixed != null) {
                params += "p5" to if (shapeFixed!!) "t" else "f"
            }
            if (shapeSleeping != null) {
                params += "p6" to if (shapeSleeping!!) "t" else "f"
            }
            if (shapeDensity != null) {
                params += "p7" to shapeDensity.toString()
            }
            if (shapeColor != null) {
                params += "p8" to shapeColor.toString()
            }
            if (shapeOutline != null) {
                params += "p9" to shapeOutline.toString()
            }
            if (shapeOpacity != null) {
                params += "p10" to shapeOpacity.toString()
            }
            if (shapeCollision != null) {
                params += "p11" to shapeCollision!!.number.toString()
            }
            if (shapeInnerCutout != null) {
                params += "p12" to shapeInnerCutout!!.toString()
            }
            if (version != null) {
                params += "v" to version!!
            }
            if (characterPosition != null) {
                params += "x" to characterPosition!!.x.toString()
                params += "y" to characterPosition!!.y.toString()
            }
            if (character != null) {
                params += "c" to character!!.number.toString()
            }
            if (forceCharacter != null) {
                params += "f" to if (forceCharacter!!) "t" else "f"
            }
            if (hideVehicle != null) {
                params += "h" to if (hideVehicle!!) "t" else "f"
            }
            if (backgroundType != null) {
                params += "bg" to backgroundType!!.number.toString()
            }
            if (backgroundColor != null) {
                params += "bgc" to backgroundColor.toString()
            }
            if (strokeOnly != null) {
                params += "f" to if (strokeOnly!!) "f" else "t"
            }
            if (pathId != null) {
                params += "id" to pathId.toString()
            }
            if (nodesCount != null) {
                params += "n" to nodesCount.toString()
            }
            params += unknown

            return params
        }
    }

    const val minVisibleAlpha = 2.55

    const val minVisibleArea = 2.0

    val Double.scaled get() = "%.3f".format(this)

    val Color.decimal get() = red * 256 * 256 + green * 256 + blue

    val levelBounds = Bounds(0.0, 0.0, 20000.0, 10000.0)
}
