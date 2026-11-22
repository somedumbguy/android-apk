package com.example.hello;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.ComponentActivity;
import com.example.hello.app.AppContainer;
import com.example.hello.auth.AuthProvider;
import com.example.hello.auth.User;
import com.example.hello.ui.common.Screens;
import com.example.hello.ui.common.Ui;
import com.example.hello.ui.home.HomeView;
import com.example.hello.ui.intensity.IntensitySheet;
import com.example.hello.ui.plan.PlanSheet;
import com.example.hello.ui.signin.SignInView;
import com.example.hello.ui.workout.WorkoutActivity;

/** Shows the sign-in screen or the home screen, and routes between screens. */
public class MainActivity extends ComponentActivity implements HomeView.Listener {
    private AppContainer container;
    private AuthProvider auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Screens.setUp(this);
        super.onCreate(savedInstanceState);
        container = AppContainer.from(this);
        auth = container.auth;
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        User user = auth.getCurrentUser();
        View screen = user == null
                ? SignInView.create(this, auth.isMock(), this::signIn)
                : HomeView.create(this, container.homePresenter().present(user), this);
        if (user != null) Screens.padForSystemBars(screen);
        setContentView(screen);
    }

    private void signIn() {
        auth.signIn(this, new AuthProvider.Callback() {
            @Override
            public void onSignedIn(User user) {
                render();
            }

            @Override
            public void onCancelled() {}

            @Override
            public void onError(String message) {
                Ui.toast(MainActivity.this, "Sign-in failed: " + message);
            }
        });
    }

    // ---- HomeView.Listener ----

    @Override
    public void onStartWorkout() {
        startActivity(new Intent(this, WorkoutActivity.class));
    }

    @Override
    public void onVoiceTapped() {
        startActivity(new Intent(this, WorkoutActivity.class).putExtra(WorkoutActivity.EXTRA_LISTEN, true));
    }

    @Override
    public void onPlanTapped() {
        PlanSheet.show(this, container.planPresenter(), this::render);
    }

    @Override
    public void onIntensityTapped() {
        IntensitySheet.show(this, container.intensityPresenter(), this::render);
    }

    @Override
    public void onAvatarTapped() {
        User user = auth.getCurrentUser();
        if (user == null) return;
        String message = user.email + (auth.isMock() ? "\n\nSigned in with mock authentication." : "");
        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle(user.displayName)
                .setMessage(message)
                .setPositiveButton("Sign out", (d, w) -> auth.signOut(this::render))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
