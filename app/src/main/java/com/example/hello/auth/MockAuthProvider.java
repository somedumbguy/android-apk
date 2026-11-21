package com.example.hello.auth;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hello.ui.common.Ui;

/**
 * Fake authentication for development. Shows a bottom sheet styled like Google's sign-in
 * sheet and signs in as a fixed test user. No network or Google setup needed.
 */
public final class MockAuthProvider implements AuthProvider {

    public static final User MOCK_USER = new User("mock-user-001", "Test User", "test.user@example.com");

    /** Simulated network delay, so loading states get exercised. */
    private static final long FAKE_DELAY_MS = 600;

    private final UserStore store;
    private final Handler main = new Handler(Looper.getMainLooper());

    public MockAuthProvider(UserStore store) {
        this.store = store;
    }

    @Override public User getCurrentUser() {
        return store.load();
    }

    @Override public void signIn(Activity activity, Callback callback) {
        int pad = Ui.dp(activity, 24);
        LinearLayout sheet = new LinearLayout(activity);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(pad, Ui.dp(activity, 12), pad, pad);
        sheet.addView(Ui.sheetHandle(activity, 0xFFC4C7C5));

        TextView tag = Ui.text(activity, "MOCK SIGN-IN · FOR TESTING", 11, 0xFFB3261E, Typeface.DEFAULT_BOLD);
        tag.setLetterSpacing(0.08f);
        sheet.addView(tag);

        TextView title = Ui.text(activity, "Sign in to Overload", 22, 0xFF1F1F1F);
        title.setPadding(0, Ui.dp(activity, 8), 0, Ui.dp(activity, 20));
        sheet.addView(title);

        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(Ui.avatar(activity, MOCK_USER.getInitial(), 40));
        LinearLayout names = new LinearLayout(activity);
        names.setOrientation(LinearLayout.VERTICAL);
        names.setPadding(Ui.dp(activity, 16), 0, 0, 0);
        names.addView(Ui.text(activity, MOCK_USER.displayName, 16, 0xFF1F1F1F, Typeface.DEFAULT_BOLD));
        names.addView(Ui.text(activity, MOCK_USER.email, 14, 0xFF5F6368));
        row.addView(names);
        sheet.addView(row);

        Button cont = Ui.pillButton(activity, "Continue as " + MOCK_USER.getFirstName(), 0xFF0B57D0, Color.WHITE, 0);
        LinearLayout.LayoutParams contLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(activity, 48));
        contLp.topMargin = Ui.dp(activity, 28);
        sheet.addView(cont, contLp);

        Dialog dialog = Ui.showSheet(activity, sheet, Color.WHITE);
        dialog.setOnCancelListener(d -> callback.onCancelled());
        cont.setOnClickListener(v -> {
            cont.setEnabled(false);
            cont.setText("Signing in…");
            dialog.setCancelable(false);
            main.postDelayed(() -> {
                store.save(MOCK_USER);
                dialog.dismiss();
                callback.onSignedIn(MOCK_USER);
            }, FAKE_DELAY_MS);
        });
    }

    @Override public void signOut(Runnable onComplete) {
        store.clear();
        main.post(onComplete);
    }

    @Override public boolean isMock() {
        return true;
    }
}
