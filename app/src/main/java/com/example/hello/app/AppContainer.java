package com.example.hello.app;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.hello.auth.AuthProvider;
import com.example.hello.auth.MockAuthProvider;
import com.example.hello.auth.UserStore;
import com.example.hello.data.SeedData;
import com.example.hello.data.SharedPrefsSettingsRepository;
import com.example.hello.data.SharedPrefsWorkoutRepository;
import com.example.hello.domain.forecast.Forecaster;
import com.example.hello.domain.plates.PlateMath;
import com.example.hello.domain.progression.ProgressionEngine;
import com.example.hello.domain.training.TrainingService;
import com.example.hello.ui.home.HomePresenter;
import com.example.hello.ui.intensity.IntensityPresenter;
import java.time.Clock;
import java.util.Locale;

/**
 * The composition root: the one place objects are created and wired together. Everything
 * else receives its dependencies through constructors, so it can be tested with fakes.
 *
 * To swap an implementation (real Google Sign-In, a server-backed repository, a different
 * seed), change it here and nowhere else.
 */
public final class AppContainer {
    public final Clock clock;
    public final PlateMath plates;
    public final TrainingService training;
    public final AuthProvider auth;

    public AppContainer(Context context) {
        Context app = context.getApplicationContext();
        clock = Clock.systemDefaultZone();
        plates = PlateMath.standard();
        training = new TrainingService(
                new SharedPrefsWorkoutRepository(prefs(app, "training"), SeedData.demo(), clock),
                new SharedPrefsSettingsRepository(prefs(app, "settings")),
                ProgressionEngine.standard(),
                new Forecaster(clock),
                plates,
                clock);
        auth = new MockAuthProvider(new UserStore(prefs(app, "auth")));
    }

    public HomePresenter homePresenter() {
        return new HomePresenter(training, clock, Locale.getDefault());
    }

    public IntensityPresenter intensityPresenter() {
        return new IntensityPresenter(training, Locale.getDefault());
    }

    /** Activities are created by Android, so they fetch the container from the Application. */
    public static AppContainer from(Context context) {
        return ((OverloadApp) context.getApplicationContext()).container();
    }

    private static SharedPreferences prefs(Context c, String name) {
        return c.getSharedPreferences(name, Context.MODE_PRIVATE);
    }
}
