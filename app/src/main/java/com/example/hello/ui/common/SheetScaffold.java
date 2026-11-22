package com.example.hello.ui.common;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/** The parts every bottom sheet shares: padding, handle, title, segmented choices and a Done button. */
public final class SheetScaffold {
    private SheetScaffold() {}

    /** A padded sheet with a handle and a title, ready for content. */
    public static LinearLayout start(Activity a, String title) {
        LinearLayout sheet = Ui.column(a);
        int pad = Ui.dp(a, 24);
        sheet.setPadding(pad, Ui.dp(a, 12), pad, pad);
        sheet.addView(Ui.sheetHandle(a, Palette.IDLE));
        sheet.addView(Ui.text(a, title, 22, Palette.TEXT, Palette.BLACK));
        return sheet;
    }

    /** Adds a Done button, shows the sheet, and calls {@code onClosed} however it's dismissed. */
    public static Dialog finish(Activity a, LinearLayout sheet, Runnable onClosed) {
        Button done = Ui.primaryButton(a, "Done");
        sheet.addView(done, Ui.matchWidth(a, 54, 22));
        Dialog d = Ui.showSheet(a, sheet, Palette.SURFACE);
        done.setOnClickListener(v -> d.dismiss());
        d.setOnDismissListener(di -> onClosed.run());
        return d;
    }

    /** A pill-shaped segmented control; fills {@code into} with one pill per label. */
    public static <T> void segments(
            Activity a, LinearLayout into, List<T> options, T selected, Consumer<T> onPick, Function<T, String> label) {
        into.removeAllViews();
        int inset = Ui.dp(a, 4);
        into.setPadding(inset, inset, inset, inset);
        into.setBackground(Ui.rounded(a, Palette.BG, 100));
        for (T option : options) {
            boolean on = option.equals(selected);
            TextView pill = Ui.text(a, label.apply(option), 15, on ? Palette.BG : Palette.MUTED, Typeface.DEFAULT_BOLD);
            pill.setGravity(Gravity.CENTER);
            if (on) pill.setBackground(Ui.rounded(a, Palette.LIME, 100));
            pill.setOnClickListener(v -> onPick.accept(option));
            into.addView(pill, new LinearLayout.LayoutParams(0, Ui.dp(a, 44), 1));
        }
    }
}
