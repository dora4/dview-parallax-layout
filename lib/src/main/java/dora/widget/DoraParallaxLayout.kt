package dora.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import dora.widget.parallaxlayout.R

class DoraParallaxLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var orientation: Int = 1
    private var initialScrollOffset: Float = 0f

    init {
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.DoraParallaxLayout)
            orientation = ta.getInt(R.styleable.DoraParallaxLayout_dview_pl_orientation, 1)
            initialScrollOffset = ta.getDimension(R.styleable.DoraParallaxLayout_dview_pl_initialScrollOffset, 0f)
            ta.recycle()
        }
    }

    override fun generateDefaultLayoutParams(): FrameLayout.LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        return LayoutParams(lp!!)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?) = p is LayoutParams

    override fun generateLayoutParams(attrs: AttributeSet?): FrameLayout.LayoutParams {
        return LayoutParams(context, attrs)
    }

    class LayoutParams : FrameLayout.LayoutParams {

        var translationXAttr: Float = 0f
        var translationYAttr: Float = 0f
        var scaleXAttr: Float = 1f
        var scaleYAttr: Float = 1f
        var alphaAttr: Float = 1f
        var rotationAttr: Float = 0f
        var animationStartFraction: Float = 0.5f // 默认可见50%触发动画
        var animationTriggered: Boolean = false

        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {
            attrs?.let {
                val ta = c.obtainStyledAttributes(it, R.styleable.DoraParallaxLayout_Layout)
                translationXAttr = ta.getDimension(R.styleable.DoraParallaxLayout_Layout_dview_pl_translation_x, 0f)
                translationYAttr = ta.getDimension(R.styleable.DoraParallaxLayout_Layout_dview_pl_translation_y, 0f)
                scaleXAttr = ta.getFloat(R.styleable.DoraParallaxLayout_Layout_dview_pl_scale_x, 1f)
                scaleYAttr = ta.getFloat(R.styleable.DoraParallaxLayout_Layout_dview_pl_scale_y, 1f)
                alphaAttr = ta.getFloat(R.styleable.DoraParallaxLayout_Layout_dview_pl_alpha, 1f)
                rotationAttr = ta.getFloat(R.styleable.DoraParallaxLayout_Layout_dview_pl_rotation, 0f)
                ta.recycle()
            }
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(source: ViewGroup.LayoutParams) : super(source)
    }

    fun checkChildVisibilityAndAnimate() {
        val parentRect = Rect()
        if (!getGlobalVisibleRect(parentRect)) return

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val lp = child.layoutParams as? LayoutParams ?: continue
            if (lp.animationTriggered) continue

            val childRect = Rect()
            if (!child.getGlobalVisibleRect(childRect)) continue

            val intersectRect = Rect()
            val visible = intersectRect.setIntersect(parentRect, childRect)
            if (!visible) continue

            val fraction = intersectRect.height().toFloat() / child.height
            if (fraction >= lp.animationStartFraction) {
                lp.animationTriggered = true
                startChildAnimation(child, lp)
            }
        }
    }

    private fun startChildAnimation(child: View, lp: LayoutParams) {
        child.animate()
            .translationX(lp.translationXAttr)
            .translationY(lp.translationYAttr)
            .scaleX(lp.scaleXAttr)
            .scaleY(lp.scaleYAttr)
            .alpha(lp.alphaAttr)
            .rotation(lp.rotationAttr)
            .setDuration(400)
            .start()
    }

    fun onScrollChanged() {
        checkChildVisibilityAndAnimate()
    }

    fun scrollToOffset(offset: Float) {
        if (orientation == 0) {
            scrollTo(offset.toInt(), scrollY)
        } else {
            scrollTo(scrollX, offset.toInt())
        }
        checkChildVisibilityAndAnimate()
    }
}
