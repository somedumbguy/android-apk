package com.example.hello.ui.common;

import android.graphics.Typeface;

/** App colors and fonts. */
public final class Palette {
    private Palette() {}

    public static final int BG = 0xFF0B0B0F;
    public static final int SURFACE = 0xFF16161D;
    public static final int SURFACE_2 = 0xFF22222C;
    public static final int STROKE = 0xFF24242F;
    public static final int TEXT = 0xFFF5F5F7;
    public static final int MUTED = 0xFF8B8B99;
    public static final int LIME = 0xFFD4FF3A;
    public static final int MINT = 0xFF3AFFB0;
    public static final int LIME_TINT = 0x1FD4FF3A;

    public static final int ORANGE = 0xFFFFA53A;
    public static final int IDLE = 0xFF3A3A48;
    public static final int IDLE_TEXT = 0xFF55556A;

    public static final int[] HERO_GRADIENT = {0xFF5B2BFF, 0xFF8E3DFF, 0xFFE0489A};
    public static final int[] AVATAR_GRADIENT = {0xFF8E3DFF, 0xFFE0489A};

    public static final Typeface BLACK = Typeface.create("sans-serif-black", Typeface.NORMAL);
    public static final Typeface MEDIUM = Typeface.create("sans-serif-medium", Typeface.NORMAL);
}
