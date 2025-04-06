package happy.svg

data class HappyGroup(
    val shapes: MutableList<HappyShape> = mutableListOf(),
    var isSleeping: Boolean = false,
    var isForeground: Boolean = false,
    var opacity: Int = 100,
    var isFixed: Boolean = false,
    var isFixedAngle: Boolean = false,
) : HappyWheels.Format {
    override val tag = "g"

    override fun params(param: (String, Any) -> Unit) {
        check(opacity in 0..100) { "Opacity must be between 0 and 100" }

        val bounds = shapes.map { (it.bounds ?: it.path?.bounds)!! }.reduce { a, b -> a union b }
        param("x", bounds.center.x)
        param("y", bounds.center.y)
        param("r", 0)
        param("ox", -bounds.center.x)
        param("oy", -bounds.center.y)
        param("f", isForeground)
        param("o", opacity)
        param("s", isSleeping)
        param("im", isFixed)
        param("fr", isFixedAngle)
    }

    override fun children(child: (HappyWheels.Format) -> Unit) {
        shapes.forEach(child)
    }
}