package wanted.ui;

import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Фоновая анимация лепестков сакуры для меню и ClickGUI. */
public class Petals {
    private static final Random RANDOM = new Random();

    private final List<Petal> petals = new ArrayList<>();
    private final int count;
    private long lastUpdate = System.currentTimeMillis();

    public Petals(int count) {
        this.count = count;
    }

    public void render(DrawContext context, int color) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        while (petals.size() < count) {
            petals.add(new Petal(width, height, true));
        }

        long now = System.currentTimeMillis();
        float delta = Math.min(100, now - lastUpdate) / 16f;
        lastUpdate = now;

        for (Petal petal : petals) {
            petal.tick(delta, width, height);
            int alpha = (int) (petal.alpha * 255);
            Render2D.circle(context, petal.x, petal.y, petal.size, (alpha << 24) | (color & 0xFFFFFF));
        }
    }

    private static final class Petal {
        float x;
        float y;
        float size;
        float speed;
        float drift;
        float phase;
        float alpha;

        Petal(int width, int height, boolean anywhere) {
            x = RANDOM.nextFloat() * width;
            y = anywhere ? RANDOM.nextFloat() * height : -10;
            reset(width);
        }

        void reset(int width) {
            size = 0.8f + RANDOM.nextFloat() * 1.8f;
            speed = 0.15f + RANDOM.nextFloat() * 0.45f;
            drift = (RANDOM.nextFloat() - 0.5f) * 0.4f;
            phase = RANDOM.nextFloat() * 6.28f;
            alpha = 0.15f + RANDOM.nextFloat() * 0.45f;
        }

        void tick(float delta, int width, int height) {
            phase += 0.03f * delta;
            y += speed * delta;
            x += (drift + (float) Math.sin(phase) * 0.25f) * delta;

            if (y > height + 8) {
                y = -8;
                x = RANDOM.nextFloat() * width;
                reset(width);
            }
            if (x < -8) x = width + 8;
            if (x > width + 8) x = -8;
        }
    }
}
