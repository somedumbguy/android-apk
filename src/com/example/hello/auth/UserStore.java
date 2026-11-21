package com.example.hello.auth;

import android.content.Context;
import android.content.SharedPreferences;

/** Remembers the signed-in user between app launches. Shared by all providers. */
public final class UserStore {
    private static final String PREFS = "auth";
    private final SharedPreferences prefs;

    public UserStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public User load() {
        String id = prefs.getString("id", null);
        if (id == null) return null;
        return new User(id, prefs.getString("name", null), prefs.getString("email", null));
    }

    public void save(User user) {
        prefs.edit()
                .putString("id", user.id)
                .putString("name", user.displayName)
                .putString("email", user.email)
                .apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
