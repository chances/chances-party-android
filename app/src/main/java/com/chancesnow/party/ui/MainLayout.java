package com.chancesnow.party.ui;

import android.content.Context;
import android.widget.LinearLayout;

import trikita.anvil.RenderableView;
import com.chancesnow.jedux.Action;

import static trikita.anvil.DSL.*;

import com.chancesnow.party.App;
import com.chancesnow.party.AppAction;
import com.chancesnow.party.R;

public class MainLayout extends RenderableView {

    public MainLayout(Context context) {
        super(context);
    }

    @Override
    public void view() {
        linearLayout(() -> {
            orientation(LinearLayout.VERTICAL);
            size(MATCH, MATCH);

            relativeLayout(() -> {
                size(MATCH, MATCH);
                padding(0, 0, 0, Style.activityMargin);
                backgroundResource(R.drawable.main_gradient);

                textView(() -> {
                    size(MATCH, WRAP);
                    padding(0, Style.textMargin, 0, 0);
                    gravity(CENTER_HORIZONTAL);
                    Style.fontSize(Style.headlineFontSize);
                    text(R.string.app_name);
                });

                button(() -> {
                    size(WRAP, WRAP);
                    Style.paddingStartEnd(Style.buttonPadding, Style.buttonPadding);
                    centerInParent();
                    backgroundResource(R.drawable.accent_button);
                    text(R.string.login);
                    visibility(!App.getState().loggedIn() && !App.getState().attemptingLogin());
                    onClick(v -> App.dispatch(new Action<>(AppAction.LOGIN, v.getContext())));
                });

                linearLayout(() -> {
                    orientation(LinearLayout.HORIZONTAL);
                    size(WRAP, WRAP);
                    centerInParent();
                    visibility(App.getState().attemptingLogin());

                    Style.loading();
                });

                linearLayout(() -> {
                    orientation(LinearLayout.VERTICAL);
                    size(MATCH, MATCH);

                    space(() -> {
                        size(MATCH, dip(0));
                        weight(1);
                    });

                    Style.footer();
                });
            });
        });
    }
}
