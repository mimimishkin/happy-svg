package happy.svg

import happy.svg.HappyWheels.Background
import happy.svg.HappyWheels.Character
import happy.svg.HappyWheels.decimal
import happy.svg.convert.HappyPreferences
import path.utils.math.Vec2
import java.awt.Color

data class HappyLevel(
    val info: Info = Info(),
    val shapes: Shapes = Shapes()
) : HappyWheels.Format {
    data class Info(
        var version: String = "1.97",
        var characterPosition: Vec2 = Vec2(20000.0, 10000.0),
        var character: Character = Character.IrresponsibleDad,
        var forceCharacter: Boolean = false,
        var hideVehicle: Boolean = false,
        var backgroundType: Background = Background.Color,
        var backgroundColor: Color = Color.white!!,
        var e: Int = 1,
    ) : HappyWheels.Format {
        override val tag = "info"

        override fun HappyWheels.Config.configure() {
            version = this@Info.version
            characterPosition = this@Info.characterPosition
            character = this@Info.character
            forceCharacter = this@Info.forceCharacter
            hideVehicle = this@Info.hideVehicle
            backgroundType = this@Info.backgroundType
            backgroundColor = this@Info.backgroundColor.decimal
            unknown += "e" to this@Info.e.toString()
        }
    }

    data class Shapes(
        val shapes: MutableList<HappyShape> = mutableListOf()
    ) : HappyWheels.Format {
        override val tag = "shapes"

        override fun HappyWheels.Config.configure() {
            childContents = shapes.map { it.format() }
        }
    }

    override val tag = "levelXML"

    override fun HappyWheels.Config.configure() {
        childContents += info.format()
        childContents += shapes.format()
    }
}

@DslMarker
annotation class HappyLevelBuilderDsl

@HappyLevelBuilderDsl
interface HappyLevelBuilder {
    fun info(block: HappyLevel.Info.() -> Unit)

    fun shapes(block: HappyLayer.() -> Unit)
}

fun happyLevel(block: HappyLevelBuilder.() -> Unit): HappyLevel {
    val level = HappyLevel()

    val builder = object : HappyLevelBuilder {
        override fun info(block: HappyLevel.Info.() -> Unit) {
            level.info.block()
        }

        override fun shapes(block: HappyLayer.() -> Unit) {
            HappyLayerImpl(level.shapes, preferences = HappyPreferences.default).block()
        }
    }

    builder.block()

    return level
}