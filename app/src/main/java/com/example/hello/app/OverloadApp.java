package com.example.hello.app;

import android.app.Application;

/** Creates the app's single {@link AppContainer} at startup. */
public final class OverloadApp extends Application {
    private AppContainer container;

    @Override
    public void onCreate() {
        super.onCreate();
        container = new AppContainer(this);
    }

    public AppContainer container() {
        return container;
    }
}
