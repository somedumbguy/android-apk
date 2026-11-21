package com.example.hello;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hello.auth.Auth;
import com.example.hello.auth.AuthProvider;
import com.example.hello.auth.User;

public class MainActivity extends Activity {
    private AuthProvider auth;
    private LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        auth = Auth.create(this);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.WHITE);
        int pad = Views.dp(this, 32);
        root.setPadding(pad, pad, pad, pad);
        setContentView(root);

        render();
    }

    private void render() {
        root.removeAllViews();
        User user = auth.getCurrentUser();
        if (user == null) showSignedOut(); else showSignedIn(user);
    }

    private void showSignedOut() {
        TextView title = Views.text(this, "Hello, World!", 32, 0xFF1F1F1F);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = Views.text(this, "Sign in to continue", 16, 0xFF5F6368);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, Views.dp(this, 8), 0, Views.dp(this, 32));
        root.addView(sub);

        Button signIn = Views.pillButton(this, "Sign in with Google", Color.WHITE, 0xFF1F1F1F, 0xFF747775);
        signIn.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(signIn, buttonParams());
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                auth.signIn(MainActivity.this, new AuthProvider.Callback() {
                    @Override public void onSignedIn(User user) { render(); }
                    @Override public void onCancelled() {}
                    @Override public void onError(String message) {
                        Toast.makeText(MainActivity.this, "Sign-in failed: " + message, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void showSignedIn(User user) {
        TextView avatar = Views.avatar(this, user.getInitial(), 96);
        ((LinearLayout.LayoutParams) avatar.getLayoutParams()).bottomMargin = Views.dp(this, 24);
        root.addView(avatar);

        TextView hello = Views.text(this, "Hello, " + user.getFirstName() + "!", 32, 0xFF1F1F1F);
        hello.setGravity(Gravity.CENTER);
        root.addView(hello);

        TextView email = Views.text(this, user.email, 16, 0xFF5F6368);
        email.setGravity(Gravity.CENTER);
        email.setPadding(0, Views.dp(this, 8), 0, Views.dp(this, 32));
        root.addView(email);

        Button signOut = Views.pillButton(this, "Sign out", Color.WHITE, 0xFF0B57D0, 0xFF747775);
        root.addView(signOut, buttonParams());
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                auth.signOut(new Runnable() {
                    @Override public void run() { render(); }
                });
            }
        });

        if (auth.isMock()) {
            TextView note = Views.text(this, "Signed in with mock authentication", 12, 0xFFB3261E);
            note.setGravity(Gravity.CENTER);
            note.setPadding(0, Views.dp(this, 24), 0, 0);
            root.addView(note);
        }
    }

    private LinearLayout.LayoutParams buttonParams() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Views.dp(this, 48));
    }
}
