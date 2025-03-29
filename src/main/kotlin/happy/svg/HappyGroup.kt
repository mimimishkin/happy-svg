package happy.svg

data class HappyGroup(
    val shapes: MutableList<HappyShape>,
    var isSleeping: Boolean = false,
    var isForeground: Boolean = false,
    var opacity: Int = 100,
    var isFixed: Boolean = false,
    var isFixedAngle: Boolean = false,
) : HappyWheels.Format {
    override val tag = "g"

    override fun HappyWheels.Config.configure() {
        val bounds = shapes.map { (it.bounds ?: it.path?.bounds)!! }.reduce { a, b -> a union b }
        groupCenter = bounds.center
        groupSleeping = isSleeping
        groupIsForeground = isForeground
        groupOpacity = opacity
        groupIsFixed = isFixed
        groupIsFixedAngle = isFixedAngle
        children = shapes
    }
}