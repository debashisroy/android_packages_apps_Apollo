package com.andrew.apollo.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * A custom {@link ScrollView} that is sized to be a perfect square, otherwise
 * functions like a typical {@link ScrollView}.
 * 
 * @author Debashis Roy (debashis.dr@gmail.com)
 */
public class SquaerScrollView extends ScrollView {

    public SquaerScrollView(Context context) {
        super(context);
    }

    public SquaerScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquaerScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMeasure(final int widthSpec, final int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        final int mSize = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(mSize, mSize);
    }
}
