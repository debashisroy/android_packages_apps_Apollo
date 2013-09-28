package com.andrew.apollo.widgets;

import static com.andrew.apollo.utils.PreferenceUtils.ENABLE_SONG_LYRICS;
import static com.andrew.apollo.utils.PreferenceUtils.SHOW_ALBUM_COVER_AS_LYRICS_BG;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ScrollView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.andrew.apollo.cache.LyricsFetcher;
import com.andrew.apollo.utils.ApolloUtils;
import com.andrew.apollo.utils.PreferenceUtils;

public class LyricsSwitcher extends TextSwitcher implements OnSharedPreferenceChangeListener {

    private PreferenceUtils mPrerences;
    private LyricsFetcher mLyricsFetcher;

    public LyricsSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public LyricsSwitcher(Context context) {
        super(context);
        initialize();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        PreferenceUtils.getInstance(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (ENABLE_SONG_LYRICS.equals(key)) {
            if (sharedPreferences.getBoolean(ENABLE_SONG_LYRICS, true)) {
                ((ScrollView) getParent()).setVisibility(VISIBLE);
                mLyricsFetcher.loadCurrentLyrics(this);
            } else {
                setText("");
                ((ScrollView) getParent()).setVisibility(INVISIBLE);
            }
        }
        if (SHOW_ALBUM_COVER_AS_LYRICS_BG.equals(key)) {
            animateSetBackgroundColor(getBackgroundColor());
        }
    }

    @Override
    public void setText(CharSequence text) {
        super.setText(text);
        ((ScrollView) getParent()).scrollTo(0, 0);
    }

    protected ViewFactory createFactory() {
        return new ViewFactory() {
            @Override
            public View makeView() {
                TextView textView = new TextView(LyricsSwitcher.this.getContext());
                textView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                // textView.setTextSize(36);
                textView.setTextColor(Color.argb(255, 255, 255, 255));
                return textView;
            }
        };
    }

    protected void initialize() {
        mPrerences = PreferenceUtils.getInstance(getContext());
        mLyricsFetcher = LyricsFetcher.getInstance(getContext());

        Animation in = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        setInAnimation(in);
        setOutAnimation(out);

        setFactory(createFactory());
    }

    /**
     * Animate background color transition.
     * 
     * @param bgColor Background color.
     */
    public void animateSetBackgroundColor(Integer bgColor) {
        if (!mPrerences.isShowCoverAsLyricsBackground()) {
            bgColor = ApolloUtils.setColorAlpha(bgColor, 255);
        }
        Integer colorFrom = getBackgroundColor();
        if (colorFrom.equals(bgColor)) {
            return;
        }
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, bgColor);
        colorAnimation.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                setBackgroundColor((Integer) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }

    public int getBackgroundColor() {
        Drawable background = getBackground();
        if (background instanceof ColorDrawable) {
            return ((ColorDrawable) background).getColor();
        }
        return 0;
    }

}
