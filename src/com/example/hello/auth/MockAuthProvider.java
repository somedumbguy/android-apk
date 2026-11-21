package com.example.hello.auth;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hello.Views;

/**
 * Fake authentication for development. Shows a bottom sheet styled like Google's
 * sign-in sheet and signs in as a fixed test user. No network or Google setup needed.
 */
public final class MockAuthProvider implements AuthProvider {

    public static final User MOCK_USER =
            new User("mock-user-001", "Test User", "test.user@example.com");

    /** Simulated network delay, so loading states get exercised. */
    private static final long FAKE_DELAY_MS = 600;

    private final UserStore store;
    private final Handler main = new Handler(Looper.getMainLooper());

    public MockAuthProvider(Context context) {
        store = new UserStore(context);
    }

    @Override
    public User getCurrentUser() {
        return store.load();
    }

    @Override
    public void signIn(Activity activity, final Callback callback) {
        showSheet(activity, callback);
    }

    @Override
    public void signOut(final Runnable onComplete) {
        store.clear();
        main.post(onComplete);
    }

    @Override
    public boolean isMock() {
        return true;
    }

    private void showSheet(Activity activity, final Callback callback) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        int pad = Views.dp(activity, 24);
        LinearLayout sheet = new LinearLayout(activity);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(pad, Views.dp(activity, 12), pad, pad);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        float r = Views.dp(activity, 28);
        bg.setCornerRadii(new float[] {r, r, r, r, 0, 0, 0, 0});
        sheet.setBackground(bg);

        // Drag handle
        View handle = new View(activity);
        GradientDrawable handleBg = new GradientDrawable();
        handleBg.setColor(0xFFC4C7C5);
        handleBg.setCornerRadius(Views.dp(activity, 2));
        handle.setBackground(handleBg);
        LinearLayout.LayoutParams handleLp =
                new LinearLayout.LayoutParams(Views.dp(activity, 32), Views.dp(activity, 4));
        handleLp.gravity = Gravity.CENTER_HORIZONTAL;
        handleLp.bottomMargin = Views.dp(activity, 16);
        sheet.addView(handle, handleLp);

        TextView tag = Views.text(activity, "MOCK SIGN-IN · FOR TESTING", 11, 0xFFB3261E);
        tag.setTypeface(Typeface.DEFAULT_BOLD);
        tag.setLetterSpacing(0.08f);
        sheet.addView(tag);

        TextView title = Views.text(activity, "Sign in to Hello World", 22, 0xFF1F1F1F);
        title.setPadding(0, Views.dp(activity, 8), 0, Views.dp(activity, 20));
        sheet.addView(title);

        // Account row
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(Views.avatar(activity, MOCK_USER.getInitial(), 40));
        LinearLayout names = new LinearLayout(activity);
        names.setOrientation(LinearLayout.VERTICAL);
        names.setPadding(Views.dp(activity, 16), 0, 0, 0);
        TextView name = Views.text(activity, MOCK_USER.displayName, 16, 0xFF1F1F1F);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        names.addView(name);
        names.addView(Views.text(activity, MOCK_USER.email, 14, 0xFF5F6368));
        row.addView(names);
        sheet.addView(row);

        final Button cont = Views.pillButton(activity,
                "Continue as " + MOCK_USER.getFirstName(), 0xFF0B57D0, Color.WHITE, 0);
        LinearLayout.LayoutParams contLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Views.dp(activity, 48));
        contLp.topMargin = Views.dp(activity, 28);
        sheet.addView(cont, contLp);

        cont.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                cont.setEnabled(false);
                cont.setText("Signing in…");
                dialog.setCancelable(false);
                main.postDelayed(new Runnable() {
                    @Override public void run() {
                        store.save(MOCK_USER);
                        dialog.dismiss();
                        callback.onSignedIn(MOCK_USER);
                    }
                }, FAKE_DELAY_MS);
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override public void onCancel(DialogInterface d) { callback.onCancelled(); }
        });

        dialog.setContentView(sheet);
        Window w = dialog.getWindow();
        w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        w.setGravity(Gravity.BOTTOM);
        w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        w.setWindowAnimations(android.R.style.Animation_InputMethod);
        dialog.show();
    }
}
