package com.example.hello.ui.common;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Shared card-style components used across screens. */
public final class Cards {
    private Cards() {}

    /** A dark rounded card with a subtle outline. */
    public static LinearLayout card(Context c) {
        LinearLayout card = Ui.column(c);
        int pad = Ui.dp(c, 18);
        card.setPadding(pad, pad, pad, pad);
        card.setBackground(cardBackground(c));
        return card;
    }

    public static GradientDrawable cardBackground(Context c) {
        GradientDrawable bg = Ui.rounded(c, Palette.SURFACE, 24);
        bg.setStroke(Ui.dp(c, 1), Palette.STROKE);
        return bg;
    }

    /** Highlights a card's outline, e.g. for a new PR. */
    public static void outline(View card, int color) {
        ((GradientDrawable) card.getBackground()).setStroke(Ui.dp(card.getContext(), 1.5f), color);
    }

    public static TextView label(Context c, String s) {
        return Ui.text(c, s, 13, Palette.MUTED, Palette.MEDIUM);
    }

    /** A big number with a smaller unit on the same baseline, e.g. "190 lb". */
    public static LinearLayout numberWithUnit(
            Context c, String number, float sp, String unit, float unitSp, int color, int unitColor) {
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBaselineAligned(true);
        TextView n = Ui.text(c, number, sp, color, Palette.BLACK);
        n.setIncludeFontPadding(false);
        row.addView(n);
        TextView u = Ui.text(c, unit, unitSp, unitColor, Palette.MEDIUM);
        u.setPadding(Ui.dp(c, 6), 0, 0, 0);
        row.addView(u);
        return row;
    }

    /** A section heading with an optional link on the right. */
    public static LinearLayout sectionTitle(Context c, String title, String action, View.OnClickListener onAction) {
        LinearLayout row = Ui.row(c);
        row.addView(Ui.text(c, title, 20, Palette.TEXT, Palette.BLACK), Ui.fill());
        if (action != null) {
            TextView link = Ui.text(c, action, 14, Palette.LIME, Palette.MEDIUM);
            link.setOnClickListener(onAction);
            row.addView(link);
        }
        return row;
    }

    /** Fades content out at the bottom of the screen, behind a floating button. */
    public static View bottomScrim(Context c, int heightDp) {
        View scrim = new View(c);
        scrim.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP, new int[] {Palette.BG, Palette.BG, Palette.BG & 0x00FFFFFF}));
        scrim.setLayoutParams(
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, heightDp), Gravity.BOTTOM));
        return scrim;
    }
}
