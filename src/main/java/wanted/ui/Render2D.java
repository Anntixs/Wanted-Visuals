package wanted.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;

/** Примитивы для отрисовки современного GUI: скруглённые панели, градиенты, кольца. */
public final class Render2D {
    private static final int ARC_STEPS = 8;

    private Render2D() {
    }

    private static void beginBlend() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
    }

    private static void endBlend() {
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /** Прямоугольник со скруглёнными углами. */
    public static void roundedRect(DrawContext context, float x, float y, float width, float height,
                                   float radius, int color) {
        roundedGradient(context, x, y, width, height, radius, color, color);
    }

    /** Скруглённый прямоугольник с вертикальным градиентом. */
    public static void roundedGradient(DrawContext context, float x, float y, float width, float height,
                                       float radius, int topColor, int bottomColor) {
        if (width <= 0 || height <= 0) return;
        radius = Math.min(radius, Math.min(width, height) / 2f);

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        beginBlend();
        BufferBuilder buffer = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        float centerX = x + width / 2f;
        float centerY = y + height / 2f;
        buffer.vertex(matrix, centerX, centerY, 0f).color(Theme.lerpColor(topColor, bottomColor, 0.5f));

        for (int i = 0; i <= 4 * ARC_STEPS; i++) {
            int corner = Math.min(3, i / ARC_STEPS);
            int stepInCorner = i - corner * ARC_STEPS;
            double angle = Math.toRadians(corner * 90.0 + (90.0 / ARC_STEPS) * stepInCorner);

            float cx;
            float cy;
            switch (corner) {
                case 0 -> { cx = x + width - radius; cy = y + height - radius; }
                case 1 -> { cx = x + radius; cy = y + height - radius; }
                case 2 -> { cx = x + radius; cy = y + radius; }
                default -> { cx = x + width - radius; cy = y + radius; }
            }

            float px = cx + (float) Math.cos(angle) * radius;
            float py = cy + (float) Math.sin(angle) * radius;
            float t = height <= 0 ? 0f : (py - y) / height;
            buffer.vertex(matrix, px, py, 0f).color(Theme.lerpColor(topColor, bottomColor, t));
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endBlend();
    }

    /** Контур скруглённого прямоугольника (рисуется как 4 тонкие полосы + углы). */
    public static void roundedOutline(DrawContext context, float x, float y, float width, float height,
                                      float radius, float thickness, int color) {
        roundedRect(context, x, y, width, thickness, thickness / 2f, color);
        roundedRect(context, x, y + height - thickness, width, thickness, thickness / 2f, color);
        roundedRect(context, x, y + radius * 0.5f, thickness, height - radius, thickness / 2f, color);
        roundedRect(context, x + width - thickness, y + radius * 0.5f, thickness, height - radius, thickness / 2f, color);
    }

    /** Горизонтальный градиентный прямоугольник без скруглений. */
    public static void horizontalGradient(DrawContext context, float x, float y, float width, float height,
                                          int leftColor, int rightColor) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        beginBlend();
        BufferBuilder buffer = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x, y, 0f).color(leftColor);
        buffer.vertex(matrix, x, y + height, 0f).color(leftColor);
        buffer.vertex(matrix, x + width, y + height, 0f).color(rightColor);
        buffer.vertex(matrix, x + width, y, 0f).color(rightColor);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endBlend();
    }

    /** Круг (используется для точек, лепестков, «переключателей»). */
    public static void circle(DrawContext context, float centerX, float centerY, float radius, int color) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        beginBlend();
        BufferBuilder buffer = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, centerX, centerY, 0f).color(color);
        for (int i = 0; i <= 24; i++) {
            double angle = Math.PI * 2 * i / 24.0;
            buffer.vertex(matrix,
                    centerX + (float) Math.cos(angle) * radius,
                    centerY + (float) Math.sin(angle) * radius, 0f).color(color);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endBlend();
    }

    /** Мягкая «тень» под панелью — несколько полупрозрачных слоёв. */
    public static void shadow(DrawContext context, float x, float y, float width, float height, float radius) {
        for (int i = 6; i > 0; i--) {
            int alpha = (int) (10 + i * 2);
            roundedRect(context, x - i, y - i, width + i * 2, height + i * 2, radius + i,
                    (alpha << 24));
        }
    }

    public static boolean hovered(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    /** Плавное приближение значения к цели, независимое от FPS-скачков. */
    public static float approach(float current, float target, float speed) {
        return current + (target - current) * Math.min(1f, speed);
    }
}
