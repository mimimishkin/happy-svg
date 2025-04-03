import happy.svg.HappyArt
import happy.svg.HappyCircle
import happy.svg.HappyGroup
import happy.svg.HappyLevel
import happy.svg.HappyPath
import happy.svg.HappyPolygon
import happy.svg.HappyRectangle
import happy.svg.HappyTriangle
import happy.svg.HappyWheels
import happy.svg.art
import happy.svg.circle
import happy.svg.clip
import happy.svg.convert.HappyPreferences
import happy.svg.group
import happy.svg.happyLevel
import happy.svg.paint.HappyLinearGradient
import happy.svg.paint.HappyRadialGradient
import happy.svg.paint.art
import happy.svg.paint.picture
import happy.svg.polygon
import happy.svg.rectangle
import happy.svg.scale
import happy.svg.transform
import happy.svg.translate
import happy.svg.triangle
import path.utils.math.Transforms
import path.utils.math.Transforms.AspectRatio
import path.utils.math.Vec2
import path.utils.paths.Bounds
import path.utils.paths.CapMode
import path.utils.paths.outline
import path.utils.paths.polygon
import path.utils.paths.spiral
import path.utils.paths.star
import path.utils.paths.toSvg
import java.awt.Color
import java.io.File
import kotlin.test.Test

class GeneralTests {
    /**
     * Manual inspection points:
     * - Output should be well formated and successfully read by Happy Wheels
     * - Character should be in (100, 100)
     * - Background should be light-cyan
     */
    @Test
    fun `level creation`() {
        val level = HappyLevel()
        level.info.character = HappyWheels.Character.MopedCouple
        level.info.characterPosition = Vec2(100.0, 100.0)
        level.info.backgroundType = HappyWheels.Background.Color
        level.info.backgroundColor = Color(0x9DFFCB)
        level.info.hideVehicle = true

        println(level.format())
    }

    /**
     * Manual inspection points:
     * - Output should be well formated and successfully read by Happy Wheels
     * - 5 shapes should be drawn, stacked on top of each other
     */
    @Test
    fun `shapes classes formating`() {
        val level = HappyLevel()

        val bounds = Bounds(150.0, 150.0, 100.0, 100.0)
        val rectangle = HappyRectangle(bounds, Color(209, 206, 30, 100))
        val circle = HappyCircle(bounds, Color(12, 108, 186, 100))
        val triangle = HappyTriangle(bounds, Color(40, 255, 120, 100))
        val polygon  = HappyPolygon(HappyPath(polygon(bounds.cx, bounds.cy, bounds.w / 2, tips = 7)), Color(255, 101, 180, 100))
        val art = HappyArt(HappyPath(star(bounds.cx, bounds.cy, bounds.w / 2)), Color(122, 40, 209, 100))

        level.shapes += rectangle
        level.shapes += circle
        level.shapes += triangle
        level.shapes += polygon
        level.shapes += art

        println(level.format())
    }

    /**
     * Manual inspection points:
     * - Art should be drawn correctly
     */
    @Test
    fun `small art drawing`() {
        val level = HappyLevel()
        val art = HappyArt(HappyPath(star(10.0, 10.0, 1.0)), Color(122, 40, 209, 100))
        level.shapes += art
        println(level.format())
    }

    /**
     * Manual inspection points:
     * - Character should be in (100, 100)
     * - Background should be light-cyan
     * - 5 shapes should be drawn, stacked on top of each other
     * - Shapes must in one group
     */
    @Test
    fun `level dsl working`() {
        val level = happyLevel {
            info {
                character = HappyWheels.Character.MopedCouple
                characterPosition = Vec2(100.0, 100.0)
                backgroundType = HappyWheels.Background.Color
                backgroundColor = Color(0x9DFFCB)
                hideVehicle = true
            }

            content {
                val bounds = Bounds(150.0, 150.0, 100.0, 100.0)

                group {
                    layer(isFixed = false, isSleeping = true) {
                        rectangle(bounds, Color(209, 206, 30, 100))
                        circle(bounds, Color(12, 108, 186, 100))
                        triangle(bounds, Color(40, 255, 120, 100))
                        polygon(polygon(bounds.cx, bounds.cy, bounds.w / 2, tips = 7), Color(255, 101, 180, 100))
                        art(star(bounds.cx, bounds.cy, bounds.w / 2), Color(122, 40, 209, 100))
                    }
                }
            }
        }

        println(level.format())
    }

    /**
     * Manual inspection points:
     * - 5 shapes should be drawn
     * - Rectangle should in the top-left corner
     * - Circle should be next to rectangle
     * - Triangle should be bigger and next to circle
     * - Polygon should be even bigger and next to triangle, there shouldn't be a big offset between them
     * - Star should normal-sized and on top of rectangle
     * - Triangle, polygon and star must be in one group
     */
    @Test
    fun `level dsl transforming`() {
        val level = happyLevel {
            content {
                val bounds = Bounds(0.0, 0.0, 100.0, 100.0)
                rectangle(bounds, Color(209, 206, 30, 100))

                translate(100.0, 100.0) {
                    circle(bounds, Color(12, 108, 186, 100))

                    group {
                        layer(isFixed = false, isSleeping = true) {
                            translate(100.0, 100.0) {
                                scale(2.0) {
                                    triangle(bounds, Color(40, 255, 120, 100))

                                    transform(Transforms.translate(100.0, 100.0)) {
                                        scale(4.0) {
                                            polygon(polygon(bounds.cx, bounds.cy, bounds.w / 2, tips = 7), Color(255, 101, 180, 100))
                                        }

                                        art(star(bounds.cx, bounds.cy, bounds.w / 2), Color(122, 40, 209, 100), ignoreLayer = true)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        println(level.format())
    }

    /**
     * Manual inspection points:
     * - Strange figure from four visual parts should be drawn
     * - Rectangular polygon should be drawn in the top-left corner
     * - Quadrant-like polygon should be drawn in the bottom-left corner
     * - Several polygons should form clipped ring part in the top-right corner
     * - Clipped triangle-like polygon should be drawn in the bottom-right corner
     * - The quadrant and trimmed circle should appear smooth
     */
    @Test
    fun `level dsl clipping`() {
        val level = happyLevel {
            content {
                layer {
                    val bounds = Bounds(0.0, 0.0, 100.0, 100.0)

                    clip(Bounds(x = 0.0, y = 0.0, w = 50.0, h = 50.0)) {
                        rectangle(bounds, Color(209, 206, 30, 100))
                    }

                    clip(Bounds(x = 0.0, y = 50.0, w = 50.0, h = 50.0)) {
                        circle(bounds, Color(12, 108, 186, 100))
                    }

                    clip(Bounds(x = 50.0, y = 0.0, w = 50.0, h = 50.0)) {
                        circle(bounds, Color(12, 108, 186, 100), innerCutout = 50f)
                    }

                    clip(Bounds(x = 50.0, y = 50.0, w = 50.0, h = 50.0)) {
                        triangle(bounds, Color(40, 255, 120, 100))
                    }
                }
            }
        }

        println(level.format())
    }

    /**
     * Manual inspection points:
     * - Two figure should be drawn
     * - The left one should be a star filled with linear gradient (from magenta to red)
     * - The right one should be a spiral filled with radial gradient (magenta in center, red only in left-bottom side)
     * - There should not be any visual imperfections
     */
    @Test
    fun `gradient painting`() {
        val level = happyLevel(preferences = HappyPreferences.default/*.copy(additionalGradientPartSize = 2.0)*/) {
            content {
                val stops = listOf(
                    0.0 to Color(181, 148, 255, 255),
                    0.5 to Color(255, 109, 243, 255),
                    0.8 to Color(255, 0, 0, 255),
                )

                val linearGradient = HappyLinearGradient(
                    start = Vec2(0.0, 0.0),
                    end = Vec2(500.0, 500.0),
                    stops = stops,
                )

                val radialGradient = HappyRadialGradient(
                    center = Vec2(800.0, 250.0),
                    focus = Vec2(870.0, 300.0),
                    radius = 270.0,
                    stops = stops,
                )

                group {
                    val star = star(cx = 300.0, cy = 300.0, outRadius = 250.0)
                    art(star, linearGradient)
                }

                group {
                    val spiralBone = spiral(start = Vec2(800.0, 300.0), end = Vec2(820.0, 310.0), coils = 7)
                    val spiral = spiralBone.outline(9.0, cap = CapMode.Round)
                    art(spiral, radialGradient)
                }
            }
        }

        println(level.format())
    }

    /**
     * Manual inspection points:
     * - Eleven characters should be drawn
     * - There should not be any visual imperfections
     */
    @Test
    fun `svg painting`() {
        val level = happyLevel {
            content {
                val characters = listOf(
                    "characters/EffectiveShopper.svg",
                    "characters/ExplorerGuy.svg",
                    "characters/HelicopterMan.svg",
                    "characters/IrresponsibleDad.svg",
                    "characters/IrresponsibleMom.svg",
                    "characters/LawnmowerMan.svg",
                    "characters/MopedCouple.svg",
                    "characters/PogostickMan.svg",
                    "characters/SantaClaus.svg",
                    "characters/SegwayGuy.svg",
                    "characters/WheelchairGuy.svg",
                ).map { GeneralTests::class.java.getResourceAsStream(it)!! }


                val bounds = Bounds(100.0, 100.0, 1000.0, 1000.0)
                for ((i, stream) in characters.withIndex()) {
                    group {
                        val viewport = bounds.copy(x = 100.0 + i * 1000.0)
                        picture(stream.buffered(), viewport, AspectRatio.XMidYMidMeet)
                    }
                }
            }
        }

        println(level.format())
    }

    @Test
    fun `raster painting`() {
        val level = happyLevel {
            content {
                val emoji = GeneralTests::class.java.getResourceAsStream("emoji.png")!!

                preferences.colorCounts = 128
                picture(emoji)
            }
        }

        println(level.format())
    }
}