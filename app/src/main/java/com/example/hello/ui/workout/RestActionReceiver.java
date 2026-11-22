package com.example.hello.ui.workout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.hello.app.AppContainer;

/** Handles the rest notification's buttons and the end-of-rest alarm. */
public final class RestActionReceiver extends BroadcastReceiver {
    static final String ACTION_DONE = "com.example.hello.rest.DONE";
    static final String ACTION_ADD = "com.example.hello.rest.ADD";
    static final String ACTION_SKIP = "com.example.hello.rest.SKIP";
    static final String ACTION_FINISHED = "com.example.hello.rest.FINISHED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        AppContainer app = AppContainer.from(context);
        RestController rest = app.restController();
        switch (action) {
            case ACTION_DONE:
                app.quickLog(rest).logNextSet(app.training.nextPlan());
                break;
            case ACTION_ADD:
                rest.addSeconds(30);
                break;
            case ACTION_SKIP:
                rest.stop();
                break;
            case ACTION_FINISHED:
                // Ignore a stale alarm, e.g. one from before the rest was extended or skipped.
                if (rest.isFinished()) {
                    String next = rest.next();
                    rest.stop();
                    app.restNotifications.showRestOver(next);
                }
                break;
            default:
                break;
        }
    }
}
