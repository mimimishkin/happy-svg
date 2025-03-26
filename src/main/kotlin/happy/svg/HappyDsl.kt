package happy.svg

import path.utils.paths.Bounds
import path.utils.paths.close
import path.utils.paths.lineTo
import path.utils.paths.moveTo
import path.utils.paths.mutablePath
import java.awt.Color


@DslMarker
annotation class HappyLevelBuilderDsl

@HappyLevelBuilderDsl
interface HappyLevelBuilder {
    fun info(block: HappyLevel.Info.() -> Unit)

    fun shapes(block: HappyLayerBuilder.() -> Unit)
}

fun happyLevel(block: HappyLevelBuilder.() -> Unit): HappyLevel {
    val level = HappyLevel()

    val builder = object : HappyLevelBuilder {
        override fun info(block: HappyLevel.Info.() -> Unit) {
            level.info.block()
        }

        override fun shapes(block: HappyLayerBuilder.() -> Unit) {
            HappyLayerBuilderImpl(level.shapes).block()
        }
    }

    builder.block()

    return level
}