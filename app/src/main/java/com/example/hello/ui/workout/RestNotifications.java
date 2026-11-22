package com.example.hello.ui.workout;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.example.hello.R;
import com.example.hello.ui.common.Palette;
import java.util.Arrays;

/**
 * The rest timer outside the app: a live countdown notification with Done / +30s / Skip
 * buttons, and an alarm that alerts when rest is over even if the app was closed. Android
 * draws the countdown itself, so no background service runs.
 */
public final class RestNotifications implements RestController.Notifier {
    static final String CHANNEL_REST = "rest";
    static final String CHANNEL_DONE = "rest_done";
    private static final int ID_REST = 1;
    private static final int ID_DONE = 2;
    private static final long[] BUZZ = {0, 220, 120, 220};

    private final Context app;

    public RestNotifications(Context context) {
        app = context.getApplicationContext();
        NotificationChannel rest =
                new NotificationChannel(CHANNEL_REST, "Rest timer", NotificationManager.IMPORTANCE_LOW);
        rest.setDescription("Countdown between sets, with buttons to log your next set");
        rest.setShowBadge(false);
        NotificationChannel done =
                new NotificationChannel(CHANNEL_DONE, "Rest over", NotificationManager.IMPORTANCE_HIGH);
        done.setDescription("Alerts you when it's time for your next set");
        done.enableVibration(true);
        done.setVibrationPattern(BUZZ);
        app.getSystemService(NotificationManager.class).createNotificationChannels(Arrays.asList(rest, done));
    }

    @Override
    public void showRest(long endsAtMillis, String next) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(app);
        nm.cancel(ID_DONE);
        scheduleAlarm(endsAtMillis);
        post(
                ID_REST,
                new NotificationCompat.Builder(app, CHANNEL_REST)
                        .setSmallIcon(R.drawable.ic_stat_rest)
                        .setColor(Palette.LIME)
                        .setContentTitle("Resting")
                        .setContentText("Next: " + next)
                        .setWhen(endsAtMillis)
                        .setShowWhen(true)
                        .setUsesChronometer(true)
                        .setChronometerCountDown(true)
                        .setOngoing(true)
                        .setOnlyAlertOnce(true)
                        .setSilent(true)
                        .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentIntent(openWorkout())
                        .addAction(0, "✓ Done", action(RestActionReceiver.ACTION_DONE))
                        .addAction(0, "+30s", action(RestActionReceiver.ACTION_ADD))
                        .addAction(0, "Skip", action(RestActionReceiver.ACTION_SKIP))
                        .build());
    }

    @Override
    public void cancel() {
        NotificationManagerCompat.from(app).cancel(ID_REST);
        app.getSystemService(AlarmManager.class).cancel(action(RestActionReceiver.ACTION_FINISHED));
    }

    /** Called by the alarm: buzz, and show a heads-up notification for the next set. */
    void showRestOver(String next) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(app);
        nm.cancel(ID_REST);
        Vibrator v = app.getSystemService(Vibrator.class);
        if (v != null) v.vibrate(VibrationEffect.createWaveform(BUZZ, -1));
        post(
                ID_DONE,
                new NotificationCompat.Builder(app, CHANNEL_DONE)
                        .setSmallIcon(R.drawable.ic_stat_rest)
                        .setColor(Palette.LIME)
                        .setContentTitle("Rest over. Let's go!")
                        .setContentText(next)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setAutoCancel(true)
                        .setContentIntent(openWorkout())
                        .build());
    }

    private void scheduleAlarm(long atMillis) {
        AlarmManager am = app.getSystemService(AlarmManager.class);
        PendingIntent finished = action(RestActionReceiver.ACTION_FINISHED);
        // Exact when the user allows it; otherwise Android may deliver it a little late.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, finished);
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, finished);
        }
    }

    /** Posts a notification if the user allows them (Android 13+ asks); otherwise does nothing. */
    private void post(int id, Notification n) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(app).notify(id, n);
    }

    private PendingIntent action(String name) {
        Intent i = new Intent(app, RestActionReceiver.class).setAction(name);
        return PendingIntent.getBroadcast(
                app, name.hashCode(), i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent openWorkout() {
        Intent i = new Intent(app, WorkoutActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(app, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
