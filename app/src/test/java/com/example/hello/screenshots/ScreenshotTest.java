package com.example.hello.screenshots;

import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import com.example.hello.MainActivity;
import com.example.hello.app.AppContainer;
import com.example.hello.ui.common.Palette;
import com.example.hello.ui.intensity.IntensitySheet;
import com.example.hello.ui.plan.PlanSheet;
import com.example.hello.ui.workout.WorkoutActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadows.ShadowDialog;

/**
 * Renders the real screens on a simulated Pixel-sized phone and saves them as PNGs in
 * app/build/screenshots, so they can be reviewed without a device. CI uploads them with
 * every pull request.
 */
@RunWith(RobolectricTestRunner.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w412dp-h892dp-420dpi")
public class ScreenshotTest {
    static final File OUT = new File("build/screenshots");

    private static void signIn() {
        Context c = RuntimeEnvironment.getApplication();
        c.getSharedPreferences("auth", Context.MODE_PRIVATE)
                .edit()
                .putString("id", "mock-user-001")
                .putString("name", "Test User")
                .putString("email", "test.user@example.com")
                .commit();
    }

    /** Lets entrance animations and the chart animations finish. */
    private static void settle() {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(3));
    }

    @Test
    public void signInScreen() throws IOException {
        Activity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        settle();
        save(a.getWindow().getDecorView(), "01-sign-in");
    }

    @Test
    public void homeScreen() throws IOException {
        signIn();
        Activity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        settle();
        View root = a.getWindow().getDecorView();
        save(root, "02-home");
        saveFullLength(findScrollView(root), "02-home-full");
    }

    @Test
    public void intensitySheet() throws IOException {
        signIn();
        Activity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        IntensitySheet.show(a, AppContainer.from(a).intensityPresenter(), () -> {});
        settle();
        Dialog d = ShadowDialog.getLatestDialog();
        save(d.getWindow().getDecorView(), "03-intensity-sheet");
    }

    @Test
    public void planSheet() throws IOException {
        signIn();
        Activity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        PlanSheet.show(a, AppContainer.from(a).planPresenter(), () -> {});
        settle();
        save(ShadowDialog.getLatestDialog().getWindow().getDecorView(), "05-plan-sheet");
    }

    @Test
    public void workoutScreen() throws IOException {
        signIn();
        Activity a = Robolectric.buildActivity(WorkoutActivity.class).setup().get();
        settle();
        View root = a.getWindow().getDecorView();
        save(root, "04-workout");
        saveFullLength(findScrollView(root), "04-workout-full");
    }

    // ---- Helpers ----

    static void save(View view, String name) throws IOException {
        assertTrue(name + " has no size", view.getWidth() > 0 && view.getHeight() > 0);
        Bitmap b = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.drawColor(Palette.BG);
        view.draw(c);
        write(b, name);
    }

    /** Draws a scrolling screen's whole content, not just the part that fits. */
    static void saveFullLength(ScrollView scroll, String name) throws IOException {
        View content = scroll.getChildAt(0);
        int width = scroll.getWidth();
        content.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        content.layout(0, 0, width, content.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(width, content.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.drawColor(Palette.BG);
        content.draw(c);
        write(b, name);
    }

    private static void write(Bitmap b, String name) throws IOException {
        assertTrue(OUT.isDirectory() || OUT.mkdirs());
        try (FileOutputStream out = new FileOutputStream(new File(OUT, name + ".png"))) {
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
    }

    private static ScrollView findScrollView(View root) {
        ScrollView found = search(root);
        if (found == null) throw new AssertionError("no ScrollView found");
        return found;
    }

    private static ScrollView search(View v) {
        if (v instanceof ScrollView) return (ScrollView) v;
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                ScrollView found = search(g.getChildAt(i));
                if (found != null) return found;
            }
        }
        return null;
    }
}
