package com.example.hello;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.hello.auth.Auth;
import com.example.hello.auth.AuthProvider;
import com.example.hello.auth.User;
import com.example.hello.data.WorkoutStore;

public class MainActivity extends Activity {
    private AuthProvider auth;
    private WorkoutStore store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Palette.BG);
        getWindow().setNavigationBarColor(Palette.BG);
        getWindow().getDecorView().setSystemUiVisibility(0);

        auth = Auth.create(this);
        store = new WorkoutStore(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        User user = auth.getCurrentUser();
        if (user == null) {
            setContentView(SignInScreen.create(this, auth.isMock(), new Runnable() {
                @Override public void run() { signIn(); }
            }));
        } else {
            setContentView(HomeScreen.create(this, user, store, new HomeScreen.Listener() {
                @Override public void onAvatarTapped() { confirmSignOut(); }
                @Override public void onStartWorkout() {
                    startActivity(new Intent(MainActivity.this, WorkoutActivity.class));
                }
                @Override public void onIntensityTapped() {
                    IntensitySheet.show(MainActivity.this, store, new Runnable() {
                        @Override public void run() { render(); }
                    });
                }
            }));
        }
    }

    private void signIn() {
        auth.signIn(this, new AuthProvider.Callback() {
            @Override public void onSignedIn(User user) { render(); }
            @Override public void onCancelled() {}
            @Override public void onError(String message) {
                Toast.makeText(MainActivity.this, "Sign-in failed: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmSignOut() {
        User user = auth.getCurrentUser();
        if (user == null) return;
        String message = user.email + (auth.isMock() ? "\n\nSigned in with mock authentication." : "");
        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle(user.displayName)
                .setMessage(message)
                .setPositiveButton("Sign out", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        auth.signOut(new Runnable() {
                            @Override public void run() { render(); }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
