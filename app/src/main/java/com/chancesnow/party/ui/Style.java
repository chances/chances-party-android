package com.chancesnow.party.ui;

import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import com.chancesnow.party.R;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import trikita.anvil.Anvil;
import trikita.anvil.BaseDSL;

import static trikita.anvil.DSL.*;

public class Style {

    public final static int activityMargin = dip(16);

    public final static int headlineFontSize = sip(24);
    public final static int textMargin = dip(16);

    public final static int listIconMargin = dip(8);

    public static final int buttonHeight = dip(36);
    public static final int buttonMargin = dip(54);
    public static final int buttonPadding = dip(36);
    public static final int buttonWithIconPaddingRight = dip(40);
    public static final int buttonRadius = dip(36);

    public static void paddingStartEnd(int start, int end) {
        BaseDSL.attr(SetPaddingStart.instance, start);
        BaseDSL.attr(SetPaddingEnd.instance, end);
    }

    public static final class SetPaddingStart implements Anvil.AttrFunc<Integer> {
        public static final SetPaddingStart instance = new SetPaddingStart();

        @Override
        public void apply(View v, Integer newValue, Integer oldValue) {
            v.setPaddingRelative(
                    newValue,
                    v.getPaddingTop(),
                    v.getPaddingEnd(),
                    v.getPaddingBottom());
        }
    }

    public static final class SetPaddingEnd implements Anvil.AttrFunc<Integer> {
        public static final SetPaddingEnd instance = new SetPaddingEnd();

        @Override
        public void apply(View v, Integer newValue, Integer oldValue) {
            v.setPaddingRelative(
                    v.getPaddingStart(),
                    v.getPaddingTop(),
                    newValue,
                    v.getPaddingBottom());
        }
    }

    public static void fontSize(int size) {
        BaseDSL.attr(SetFontSize.instance, size);
    }

    public static final class SetFontSize implements Anvil.AttrFunc<Integer> {
        public static final SetFontSize instance = new SetFontSize();

        @Override
        public void apply(View v, Integer newValue, Integer oldValue) {
            if (v instanceof android.widget.TextView) {
                ((android.widget.TextView) v).setTextSize(
                        TypedValue.COMPLEX_UNIT_PX, newValue);
            }
        }
    }

    public static void loading() {
        linearLayout(() -> {
            orientation(LinearLayout.VERTICAL);
            size(WRAP, WRAP);
            gravity(CENTER);

            v(MaterialProgressBar.class, () -> {
                size(WRAP, WRAP);
                layoutGravity(CENTER_HORIZONTAL);
                indeterminate(true);

                attr((v, newValue, oldValue) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ((MaterialProgressBar) v).getDrawable().setTint(v.getResources().getColor(newValue));
                    }
                }, R.color.colorAccent);
            });

            textView(() -> {
                size(WRAP, WRAP);
                layoutGravity(CENTER_HORIZONTAL);
                text(R.string.loading);
            });
        });
    }

    public static void footer() {
        linearLayout(() -> {
            orientation(LinearLayout.HORIZONTAL);
            size(MATCH, WRAP);

            space(() -> {
                size(0, 0);
                weight(1);
            });

            linearLayout(() -> {
                orientation(LinearLayout.HORIZONTAL);
                size(WRAP, WRAP);
                layoutGravity(CENTER_VERTICAL);
                gravity(BOTTOM);

                textView(() -> {
                    size(WRAP, WRAP);
                    paddingStartEnd(0, textMargin);
                    layoutGravity(CENTER_VERTICAL);
                    text(R.string.poweredBy);
                });

                imageView(() -> {
                    size(dip(110), dip(37));
                    String spotify = Anvil.currentView().getResources().getString(R.string.spotify);
                    contentDescription(spotify);
                    imageResource(R.drawable.spotify_logo_white);
                });
            });

            space(() -> {
                size(0, 0);
                weight(1);
            });
        });
    }
}
