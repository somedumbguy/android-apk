package com.example.hello.auth;

/** The signed-in user. Same shape for the mock and the real Google provider. */
public final class User {
    public final String id;
    public final String displayName;
    public final String email;

    public User(String id, String displayName, String email) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
    }

    public String getFirstName() {
        if (displayName == null || displayName.trim().isEmpty()) return email;
        return displayName.trim().split("\\s+")[0];
    }

    public String getInitial() {
        String source = (displayName != null && !displayName.trim().isEmpty()) ? displayName.trim() : email;
        return source == null || source.isEmpty() ? "?" : source.substring(0, 1).toUpperCase();
    }
}
