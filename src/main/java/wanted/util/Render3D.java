package wanted.util;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** Отрисовка линий и боксов в мире (ESP, трейсеры). */
public final class Render3D {
    private Render3D() {
    }

    public static void drawBox(WorldRenderContext context, Box box, int argb) {
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (matrices == null || consumers == null) return;

        Vec3d cam = context.camera().getPos();
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLines());

        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);

        float a = ((argb >> 24) & 0xFF) / 255f;
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;

        net.minecraft.client.render.WorldRenderer.drawBox(matrices, buffer,
                box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, r, g, b, a);

        matrices.pop();
    }

    public static void drawLine(WorldRenderContext context, Vec3d from, Vec3d to, int argb) {
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (matrices == null || consumers == null) return;

        Vec3d cam = context.camera().getPos();
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLines());

        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);

        MatrixStack.Entry entry = matrices.peek();
        float a = ((argb >> 24) & 0xFF) / 255f;
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;

        Vec3d direction = to.subtract(from).normalize();

        buffer.vertex(entry.getPositionMatrix(), (float) from.x, (float) from.y, (float) from.z)
                .color(r, g, b, a)
                .normal(entry, (float) direction.x, (float) direction.y, (float) direction.z);
        buffer.vertex(entry.getPositionMatrix(), (float) to.x, (float) to.y, (float) to.z)
                .color(r, g, b, a)
                .normal(entry, (float) direction.x, (float) direction.y, (float) direction.z);

        matrices.pop();
    }

    /** Интерполированная позиция сущности для плавных боксов. */
    public static Vec3d lerpPos(net.minecraft.entity.Entity entity, float tickDelta) {
        return new Vec3d(
                net.minecraft.util.math.MathHelper.lerp(tickDelta, entity.prevX, entity.getX()),
                net.minecraft.util.math.MathHelper.lerp(tickDelta, entity.prevY, entity.getY()),
                net.minecraft.util.math.MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ()));
    }
}
