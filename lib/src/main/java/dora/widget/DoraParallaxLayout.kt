package dora.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import dora.widget.parallaxlayout.R

class DoraParallaxLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var orientation: Int = 0
    private var initialOffset: Int = 0
    private lateinit var scrollContainer: ViewGroup

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.DoraParallaxLayout, 0, 0).apply {
            try {
                orientation = getInt(R.styleable.DoraParallaxLayout_dview_pl_orientation, 0)
                initialOffset = getDimensionPixelSize(
                    R.styleable.DoraParallaxLayout_dview_pl_initialScrollOffset, 0
                )
            } finally {
                recycle()
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val originalChildren = children.toList()
        removeAllViews()
        scrollContainer = if (orientation == 1) {
            object : ScrollView(context) {
                override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
                    super.onScrollChanged(l, t, oldl, oldt)
                    applyParallax(l, t)
                }
            }.apply { overScrollMode = OVER_SCROLL_NEVER }
        } else {
            object : HorizontalScrollView(context) {
                override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
                    super.onScrollChanged(l, t, oldl, oldt)
                    applyParallax(l, t)
                }
            }.apply { overScrollMode = OVER_SCROLL_NEVER }
        }

        val linear = LinearLayout(context).apply {
            orientation = if (orientation == 1) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        scrollContainer.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        scrollContainer.addView(linear)
        addView(scrollContainer)

        originalChildren.forEach { linear.addView(it) }

        scrollContainer.doOnPreDraw {
            if (initialOffset != 0) {
                if (orientation == 1) scrollContainer.scrollTo(0, initialOffset)
                else (scrollContainer as HorizontalScrollView).scrollTo(initialOffset, 0)
                initialOffset = 0
            }
            applyParallax(
                scrollContainer.scrollX,
                scrollContainer.scrollY
            )
        }
    }

    private fun applyParallax(scrollX: Int, scrollY: Int) {
        val container = (scrollContainer.getChildAt(0) as? ViewGroup) ?: return
        val maxScroll = if (orientation == 1)
            (container.height - height).coerceAtLeast(1)
        else (container.width - width).coerceAtLeast(1)
        val fraction = if (orientation == 1) scrollY.toFloat() / maxScroll else scrollX.toFloat() / maxScroll

        container.children.forEach { child ->
            val lp = child.layoutParams as LayoutParams
            child.translationX = if (orientation == 1) 0f else lp.parallaxTranslationX * fraction
            child.translationY = if (orientation == 1) lp.parallaxTranslationY * fraction else 0f
            child.scaleX = 1f + (lp.parallaxScaleX - 1f) * fraction
            child.scaleY = 1f + (lp.parallaxScaleY - 1f) * fraction
            child.alpha = 1f + (lp.parallaxAlpha - 1f) * fraction
            child.rotation = lp.parallaxRotation * fraction
        }
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        return  LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?) = p is LayoutParams

    class LayoutParams : MarginLayoutParams {

        var parallaxTranslationX: Int = 0
        var parallaxTranslationY: Int = 0
        var parallaxScaleX: Float = 1f
        var parallaxScaleY: Float = 1f
        var parallaxAlpha: Float = 1f
        var parallaxRotation: Float = 0f

        constructor(c: Context, attrs: AttributeSet) : super(c, attrs) {
            c.theme.obtainStyledAttributes(
                attrs, R.styleable.DoraParallaxLayout_Layout, 0, 0
            ).apply {
                try {
                    parallaxTranslationX = getDimensionPixelOffset(
                        R.styleable.DoraParallaxLayout_Layout_dview_pl_translation_x, 0
                    )
                    parallaxTranslationY = getDimensionPixelOffset(
                        R.styleable.DoraParallaxLayout_Layout_dview_pl_translation_y, 0
                    )
                    parallaxScaleX = getFloat(
                        R.styleable.DoraParallaxLayout_Layout_dview_pl_scale_x, 1f
                    )
                    parallaxScaleY = getFloat(
                        R.styleable.DoraParallaxLayout_Layout_dview_pl_scale_y, 1f
                    )
                    parallaxAlpha = getFloat(
                        R.styleable.DoraParallaxLayout_Layout_dview_pl_alpha, 1f
                    )
                    parallaxRotation = getFloat(
                        R.styleable.DoraParallaxLayout_Layout_dview_pl_rotation, 0f
                    )
                } finally {
                    recycle()
                }
            }
        }

        constructor(width: Int, height: Int) : super(width, height)

        constructor(source: ViewGroup.LayoutParams) : super(source)
    }
}
