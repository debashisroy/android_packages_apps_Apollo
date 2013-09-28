package com.andrew.apollo.lyrics;

import com.andrew.apollo.widgets.SquaerScrollView;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

/**
 * The view used to display song lyrics.
 * 
 * @author Debashis Roy (debashis.dr@gmail.com)
 */
public class LyricsView extends SquaerScrollView implements ViewFactory {

    protected static final int TEXT_COLOR = Color.argb(255, 255, 255, 255);
    protected static final int TEXT_SIZE = 12;

    protected TextSwitcher mLyricsSwitcher;

    public LyricsView(Context context) {
        super(context);
        initView();
    }

    public LyricsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LyricsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    /**
     * Create child views.
     */
    protected void initView() {
        mLyricsSwitcher = new TextSwitcher(getContext());

        mLyricsSwitcher.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mLyricsSwitcher.setFactory(this);

        Animation in = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        mLyricsSwitcher.setInAnimation(in);
        mLyricsSwitcher.setOutAnimation(out);

        addView(mLyricsSwitcher);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View makeView() {
        TextView textView = new TextView(getContext());
        textView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        // textView.setTextSize(TEXT_SIZE);
        textView.setTextColor(TEXT_COLOR);
        return textView;
    }

    /**
     * Animate background color transition.
     * 
     * @param bgColor Background color.
     */
    public void animateSetBackgroundColor(Integer bgColor) {
        Integer colorFrom = getBackgroundColor();
        if (colorFrom.equals(bgColor)) {
            return;
        }
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, bgColor);
        colorAnimation.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mLyricsSwitcher.setBackgroundColor((Integer) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }

    /**
     * Returns the background color of the child text view.
     */
    public int getBackgroundColor() {
        Drawable background = mLyricsSwitcher.getBackground();
        if (background instanceof ColorDrawable) {
            return ((ColorDrawable) background).getColor();
        }
        return 0;
    }

    /**
     * Sets song lyrics.
     * 
     * @param lyrics The song lyrics.
     */
    public void setLyrics(String lyrics) {
        mLyricsSwitcher.setText(lyrics);
    }
}
