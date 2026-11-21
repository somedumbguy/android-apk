package com.example.hello.auth;

import android.content.Context;

/**
 * The one place that decides which authentication provider the app uses.
 *
 * To switch to real Google Sign-In, add a GoogleAuthProvider that implements
 * AuthProvider (using Credential Manager and the Web client ID from Google Cloud
 * Console), then return it here instead of MockAuthProvider.
 */
public final class Auth {
    private Auth() {}

    public static AuthProvider create(Context context) {
        return new MockAuthProvider(context.getApplicationContext());
    }
}
