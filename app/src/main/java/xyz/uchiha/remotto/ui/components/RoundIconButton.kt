package xyz.uchiha.remotto.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import xyz.uchiha.remotto.R

// to support lower apis we need to split the constructor: https://medium.com/mobile-app-development-publication/building-custom-component-with-kotlin-fc082678b080
class RoundIconButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes) {
    private var button: MaterialButton

    init {
        val layout = LayoutInflater.from(context).inflate(R.layout.round_icon_button, this, true)
        button = layout.findViewById(R.id.round_icon_button) as MaterialButton

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.RoundIconButton, 0, 0)
            val icon = context.getDrawable(typedArray.getResourceId(R.styleable.RoundIconButton_icon_RIB, R.drawable.round_keyboard_arrow_right_24))

            button.icon = icon
            typedArray.recycle()
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        button.setOnClickListener(l)
    }
}