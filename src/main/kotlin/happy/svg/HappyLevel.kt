package happy.svg

import happy.svg.HappyWheels.Background
import happy.svg.HappyWheels.Character
import happy.svg.HappyWheels.decimal
import happy.svg.convert.HappyPreferences
import path.utils.math.Vec2
import java.awt.Color

data class HappyLevel(
    val info: Info = Info(),
    val shapes: Shapes = Shapes(),
    val groups: Groups = Groups(),
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

        override fun params(param: (String, Any) -> Unit) {
            param("v", version)
            param("x", characterPosition.x)
            param("y", characterPosition.y)
            param("c", character.number)
            param("f", forceCharacter)
            param("h", hideVehicle)
            param("bg", backgroundType.number)
            param("bgc", backgroundColor.decimal)
            param("e", e)
        }
    }

    data class Shapes(
        val shapes: MutableList<HappyShape> = mutableListOf()
    ) : HappyWheels.Format, MutableList<HappyShape> by shapes {
        override val tag = "shapes"

        override val children: List<HappyWheels.Format>
            get() = shapes
    }

    data class Groups(
        val groups: MutableList<HappyGroup> = mutableListOf()
    ) : HappyWheels.Format, MutableList<HappyGroup> by groups {
        override val tag = "groups"

        override val children: List<HappyWheels.Format>
            get() = groups
    }

    override val tag = "levelXML"

    override val children: List<HappyWheels.Format>
        get() = listOfNotNull(info, shapes.takeIf { it.isNotEmpty() }, groups.takeIf { it.isNotEmpty() })
}

@DslMarker
annotation class HappyLevelBuilderDsl

@HappyLevelBuilderDsl
interface HappyLevelBuilder {
    fun info(block: HappyLevel.Info.() -> Unit)

    fun content(block: HappyLayer.() -> Unit)
}

fun happyLevel(
    preferences: HappyPreferences = HappyPreferences.default,
    block: HappyLevelBuilder.() -> Unit
): HappyLevel {
    val level = HappyLevel()
    val layerImpl = HappyLayerImpl(
        onShape = { level.shapes += it },
        onGroup = { level.groups += it },
        preferences = preferences
    )

    val builder = object : HappyLevelBuilder {
        override fun info(block: HappyLevel.Info.() -> Unit) = level.info.block()
        override fun content(block: HappyLayer.() -> Unit) = layerImpl.block()
    }

    builder.block()

    return level
}