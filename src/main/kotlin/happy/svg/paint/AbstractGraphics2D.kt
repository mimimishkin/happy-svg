package happy.svg.paint

import java.awt.*
import java.awt.RenderingHints.*
import java.awt.font.FontRenderContext
import java.awt.font.GlyphVector
import java.awt.font.TextLayout
import java.awt.geom.*
import java.awt.image.*
import java.awt.image.BufferedImage.*
import java.awt.image.renderable.RenderableImage
import java.text.AttributedCharacterIterator
import java.util.*
import javax.swing.ImageIcon
import kotlin.math.abs
import kotlin.math.min
import java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment as localGraphicsEnvironment
import java.awt.geom.AffineTransform.getRotateInstance as rotateTransform
import java.awt.geom.AffineTransform.getScaleInstance as scaleTransform
import java.awt.geom.AffineTransform.getShearInstance as shearTransform
import java.awt.geom.AffineTransform.getTranslateInstance as translateTransform

internal abstract class AbstractGraphics2D : Graphics2D() {
    companion object {
        val defaultFont: Font = Font.decode(null)
    }

    override fun create() = this

    override fun dispose() = Unit

    protected open var _transform: AffineTransform = AffineTransform()
    override fun translate(x: Int, y: Int) = translate(x.toDouble(), y.toDouble())
    override fun translate(tx: Double, ty: Double) = transform(translateTransform(tx, ty))
    override fun rotate(theta: Double) = transform(rotateTransform(theta))
    override fun rotate(theta: Double, x: Double, y: Double) = transform(rotateTransform(theta, x, y))
    override fun scale(sx: Double, sy: Double) = transform(scaleTransform(sx, sy))
    override fun shear(shx: Double, shy: Double) = transform(shearTransform(shx, shy))
    override fun transform(Tx: AffineTransform) { _transform.concatenate(Tx) }
    override fun setTransform(Tx: AffineTransform?) { _transform = Tx ?: AffineTransform() }
    override fun getTransform() = _transform.clone() as AffineTransform

    override fun draw(s: Shape) { if (_stroke != null) fill(_stroke!!.createStrokedShape(s)) }
    override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) = draw(Line2D.Double(x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble()))
    override fun drawRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int) = draw(RoundRectangle2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), arcWidth.toDouble(), arcHeight.toDouble()))
    override fun drawOval(x: Int, y: Int, width: Int, height: Int) = draw(Ellipse2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble()))
    override fun drawArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int) = draw(Arc2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), startAngle.toDouble(), arcAngle.toDouble(), Arc2D.OPEN))
    override fun drawPolyline(xPoints: IntArray?, yPoints: IntArray?, nPoints: Int) = draw(Path2D.Double().apply { for (i in 0 until  nPoints) { lineTo(xPoints!![i].toDouble(), yPoints!![i].toDouble()) } })
    override fun drawPolygon(xPoints: IntArray?, yPoints: IntArray?, nPoints: Int) = draw(Polygon(xPoints, yPoints, nPoints))

    override fun fillRect(x: Int, y: Int, width: Int, height: Int) = fill(Rectangle(x, y, width, height))
    override fun fillRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int) = fill(RoundRectangle2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), arcWidth.toDouble(), arcHeight.toDouble()))
    override fun fillOval(x: Int, y: Int, width: Int, height: Int) = fill(Ellipse2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble()))
    override fun fillArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int) = fill(Arc2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), startAngle.toDouble(), arcAngle.toDouble(), Arc2D.OPEN))
    override fun fillPolygon(xPoints: IntArray?, yPoints: IntArray?, nPoints: Int) = fill(Polygon(xPoints, yPoints, nPoints))

    protected open var _clip: Shape? = null
    override fun getClipBounds() = _clip?.bounds
    override fun clipRect(x: Int, y: Int, width: Int, height: Int) = clip(Rectangle(x, y, width, height))
    override fun setClip(x: Int, y: Int, width: Int, height: Int) { clip = Rectangle(x, y, width, height) }
    override fun setClip(clip: Shape?) { _clip = clip }
    override fun getClip(): Shape? = _clip?.let { object : Shape by it {} }
    override fun clip(s: Shape?) = _clip.let { c ->
        _clip = when {
            s == null -> c
            c == null -> s
            c is RectangularShape && s is RectangularShape -> c.frame.createIntersection(s.frame)
            c is Area -> c.also { it.intersect(s as? Area ?: Area(s)) }
            s is Area -> Area(s).also { it.intersect(Area(c)) }
            else -> Area(c).also { it.intersect(Area(s)) }
        }
    }

    override fun drawString(str: String, x: Int, y: Int) = drawString(str, x.toFloat(), y.toFloat())
    override fun drawString(str: String, x: Float, y: Float) = TextLayout(str, _font, fontRenderContext).draw(this, x, y)
    override fun drawString(iterator: AttributedCharacterIterator?, x: Int, y: Int) = drawString(iterator, x.toFloat(), y.toFloat())
    override fun drawString(iterator: AttributedCharacterIterator?, x: Float, y: Float) = TextLayout(iterator, fontRenderContext).draw(this, x, y)
    override fun drawGlyphVector(g: GlyphVector, x: Float, y: Float) = draw(g.getOutline(x, y))

    override fun drawImage(img: Image?, x: Int, y: Int, width: Int, height: Int, bgcolor: Color?, observer: ImageObserver?): Boolean {
        val old = foreground

        if (bgcolor != null) {
            foreground = bgcolor
            fillRect(x, y, width, height)
        }

        if (img != null) {
            foreground = TexturePaint(img.toBuffered(), Rectangle(x, y, width, height))
            fillRect(x, y, width, height)
        }

        foreground = old
        return true
    }
    override fun drawImage(img: Image, xform: AffineTransform, obs: ImageObserver?) = img.transform(xform, _renderingHints).run { drawImage(this, minX, minY, width, height, null, null) }
    override fun drawImage(img: Image, x: Int, y: Int, observer: ImageObserver?) = drawImage(img, x, y, img.getWidth(observer), img.getHeight(observer), null, observer)
    override fun drawImage(img: Image, x: Int, y: Int, bgcolor: Color, observer: ImageObserver?) = drawImage(img, x, y, img.getWidth(observer), img.getHeight(observer), bgcolor, observer)
    override fun drawImage(img: Image?, x: Int, y: Int, width: Int, height: Int, observer: ImageObserver?) = drawImage(img, x, y, width, height, null, observer)
    override fun drawImage(img: Image?, dx1: Int, dy1: Int, dx2: Int, dy2: Int, sx1: Int, sy1: Int, sx2: Int, sy2: Int, observer: ImageObserver?) = drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null, observer)
    override fun drawImage(img: BufferedImage, op: BufferedImageOp?, x: Int, y: Int) { drawImage(op?.filter(img, null) ?: img, x, y, null) }
    override fun drawImage(img: Image?, dx1: Int, dy1: Int, dx2: Int, dy2: Int, sx1: Int, sy1: Int, sx2: Int, sy2: Int, bgcolor: Color?, observer: ImageObserver?): Boolean {
        if (img == null) return true
        val cropped = img.toBuffered().getSubimage(min(sx1, sx2), min(sy1, sy2), abs(sx2 - sx1), abs(sy2 - sy1))
        return drawImage(cropped, min(dx1, dx2), min(dy1, dy2), abs(dx2 - dx1), abs(dy2 - dy1), bgcolor, observer)
    }
    override fun drawRenderableImage(img: RenderableImage, xform: AffineTransform) = drawRenderedImage(img.createDefaultRendering(), xform)
    override fun drawRenderedImage(img: RenderedImage, xform: AffineTransform) { drawImage(img.toBuffered(), xform, null) }

    protected open var _renderingHints: RenderingHints = RenderingHints(null)
    override fun setRenderingHint(hintKey: Key, hintValue: Any?) { _renderingHints[hintKey] = hintValue }
    override fun getRenderingHint(hintKey: Key) = _renderingHints[hintKey]
    override fun setRenderingHints(hints: MutableMap<*, *>) { _renderingHints.clear(); addRenderingHints(hints) }
    override fun addRenderingHints(hints: MutableMap<*, *>) { for ((key, value) in hints) setRenderingHint(key as Key, value) }
    override fun getRenderingHints() = _renderingHints.clone() as RenderingHints

    protected open var foreground: Paint = Color.white
    override fun getColor() = foreground as? Color
    override fun setColor(c: Color?) { if (c != null) foreground = c }
    override fun getPaint() = foreground
    override fun setPaint(p: Paint?) { if (p != null) foreground = p }

    protected open var _stroke: Stroke? = null
    override fun getStroke() = _stroke
    override fun setStroke(s: Stroke?) { _stroke = s }

    protected open var _background: Color = Color.black
    override fun getBackground() = _background
    override fun setBackground(color: Color) { _background = color }

    protected open var _composite: Composite = AlphaComposite.SrcOver
    override fun getComposite() = _composite
    override fun setComposite(comp: Composite) { _composite = comp }
    override fun setPaintMode() { _composite = AlphaComposite.SrcOver }
    override fun setXORMode(xorColor: Color) { composite = XorComposite(xorColor) }

    protected open var _font: Font = defaultFont
    override fun setFont(font: Font?) { _font = font ?: defaultFont }
    override fun getFont() = _font

    override fun hit(rect: Rectangle, s: Shape, onStroke: Boolean): Boolean {
        val toHit = if (onStroke && _stroke != null) _stroke!!.createStrokedShape(s) else s
        return _transform.createTransformedShape(toHit).intersects(rect)
    }

    override fun getDeviceConfiguration(): GraphicsConfiguration? =
        localGraphicsEnvironment().takeUnless { it.isHeadlessInstance }?.defaultScreenDevice?.defaultConfiguration

    override fun getFontRenderContext(): FontRenderContext {
        val antialiasing = _renderingHints[KEY_TEXT_ANTIALIASING]
        val fractionMetrics = _renderingHints[KEY_FRACTIONALMETRICS]
        return FontRenderContext(transform, antialiasing, fractionMetrics)
    }

    override fun getFontMetrics(f: Font): FontMetrics = BufferedImage(1, 1, TYPE_INT_ARGB_PRE).graphics.getFontMetrics(f)

    override fun clearRect(x: Int, y: Int, width: Int, height: Int) {
//        val oldMode = _composite
//        val oldForeground = foreground
//
//        composite = AlphaComposite.Src
//        foreground = background
//        fillRect(x, y, width, height)
//
//        composite = oldMode
//        foreground = oldForeground
        throw NotImplementedError()
    }

    override fun copyArea(x: Int, y: Int, width: Int, height: Int, dx: Int, dy: Int) {
        throw NotImplementedError()
    }
}

// ----------------------------------------------------------------------------------------------------------
// ------------------------------------------                      ------------------------------------------
// -----------------------                           UTILS                            -----------------------
// ------------------------------------------                      ------------------------------------------
// ----------------------------------------------------------------------------------------------------------

class XorComposite(val xorColor: Color) : Composite {
    override fun createContext(
        srcColorModel: ColorModel,
        dstColorModel: ColorModel,
        hints: RenderingHints?
    ): CompositeContext = object : CompositeContext {
        var rgba_src: IntArray? = IntArray(4)
        var rgba_dstIn: IntArray? = IntArray(4)
        var rgba_dstOut: IntArray? = IntArray(4)
        var rgba_xor: IntArray? = with(xorColor) { intArrayOf(red, green, blue, alpha) }

        override fun dispose() {
            rgba_src = null
            rgba_dstIn = null
            rgba_dstOut = null
            rgba_xor = null
        }

        override fun compose(src: Raster, dstIn: Raster, dstOut: WritableRaster) {
            val width = src.width
            val height = src.height

            for (j in 0 until height) {
                for (i in 0 until width) {
                    src.getPixel(i, j, rgba_src)
                    dstIn.getPixel(i, j, rgba_dstIn)

                    rgba_dstOut!![0] = rgba_src!![0] xor rgba_xor!![0] xor rgba_dstIn!![0]
                    rgba_dstOut!![1] = rgba_src!![1] xor rgba_xor!![1] xor rgba_dstIn!![1]
                    rgba_dstOut!![2] = rgba_src!![2] xor rgba_xor!![2] xor rgba_dstIn!![2]
                    rgba_dstOut!![3] = rgba_src!![3] xor rgba_xor!![3] xor rgba_dstIn!![3]
                    dstOut.setPixel(i, j, rgba_dstOut)
                }
            }
        }
    }
}

fun RenderedImage.toBuffered(): BufferedImage {
    if (this is BufferedImage)
        return this

    val raster = copyData(null)
    val isAlphaPremultiplied = colorModel.isAlphaPremultiplied
    val properties = propertyNames?.associateWithTo(Hashtable()) { getProperty(it) }

    return BufferedImage(colorModel, raster, isAlphaPremultiplied, properties)
}

fun Image.toBuffered(): BufferedImage {
    if (this is BufferedImage)
        return this
    if (this is RenderedImage)
        return (this as RenderedImage).toBuffered()

    // This code ensures that all the pixels in the image are loaded
    val image = ImageIcon(this).image

    // Create a buffered image with a format that's compatible with the screen
    val env = localGraphicsEnvironment()
    val buffered = if (!env.isHeadlessInstance) {
        val config = env.defaultScreenDevice.defaultConfiguration
        val transparency = if (hasAlpha) TRANSLUCENT else OPAQUE
        config.createCompatibleImage(image.width, image.height, transparency)
    } else {
        val type = if (hasAlpha) TYPE_INT_ARGB else TYPE_INT_RGB
        BufferedImage(image.width, image.height, type)
    }

    val graphics = buffered.createGraphics()
    graphics.drawImage(image, 0, 0, null)
    graphics.dispose()
    return buffered
}

val Image.width get() = getWidth(null)
val Image.height get() = getHeight(null)
val Image.hasAlpha: Boolean get() =
    if (this is BufferedImage) {
        this.colorModel.hasAlpha()
    } else {
        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
        try {
            val pg = PixelGrabber(this, 0, 0, 1, 1, false)
            pg.grabPixels()
            pg.colorModel.hasAlpha()
        } catch (e: InterruptedException) {
            false
        }
    }

fun Image.scale(width: Int, height: Int) = getScaledInstance(width, height, SCALE_SMOOTH)
fun Image.transform(xform: AffineTransform, hints: RenderingHints): BufferedImage {
    return AffineTransformOp(xform, hints).filter(toBuffered(), null)
}