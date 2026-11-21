package com.example.hello.auth;

import android.app.Activity;

/**
 * Everything the app needs from authentication. The rest of the app only uses this
 * interface, so the mock can be replaced by real Google Sign-In without other changes.
 */
public interface AuthProvider {

    interface Callback {
        void onSignedIn(User user);

        void onCancelled();

        void onError(String message);
    }

    /** The signed-in user, or null if signed out. */
    User getCurrentUser();

    /** Shows the sign-in UI and reports the result through the callback. */
    void signIn(Activity activity, Callback callback);

    /** Signs out and calls onComplete when done. */
    void signOut(Runnable onComplete);

    /** True for test providers, so the UI can label mock sessions. */
    boolean isMock();
}
