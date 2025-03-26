package happy.svg

import happy.svg.convert.HappyPreferences

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