package wanted.render;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import wanted.module.ModuleManager;
import wanted.modules.visual.KasaHatModule;

/**
 * Рисует японскую шляпу (каса) поверх головы игрока.
 * Геометрия строится процедурно: набор колец от полей к макушке.
 */
public class KasaFeatureRenderer
        extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    private static final Identifier TEXTURE = Identifier.of("wanted", "textures/entity/kasa.png");
    private static final int SEGMENTS = 24;
    private static final int RINGS = 6;

    public KasaFeatureRenderer(
            FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       AbstractClientPlayerEntity entity, float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress, float headYaw, float headPitch) {

        KasaHatModule module = ModuleManager.kasaHat();
        if (module == null || !module.isEnabled()) return;
        if (entity.isInvisible() || entity.isSpectator()) return;
        if (!shouldRenderFor(module, entity)) return;

        int usedLight = module.glow.get() ? LightmapTextureManager.MAX_LIGHT_COORDINATE : light;
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));

        matrices.push();
        getContextModel().head.rotate(matrices);
        // Приводим систему координат головы к «нормальной»: Y вверх, Z вперёд.
        matrices.scale(1f, -1f, -1f);
        matrices.translate(0f, 0.5f + module.offset.getFloat(), 0f);

        if (module.bob.get()) {
            double phase = (System.currentTimeMillis() % 4000L) / 4000.0 * Math.PI * 2;
            matrices.translate(0f, (float) Math.sin(phase) * 0.02f, 0f);
        }
        if (module.spin.get()) {
            float angle = (float) ((System.currentTimeMillis() % 36000L) / 100.0) * module.spinSpeed.getFloat();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));
        }

        float radius = 0.45f * module.size.getFloat();
        float height = module.height.getFloat();
        int color = module.color.getArgb();

        switch (module.shape.get()) {
            case "Jingasa" -> buildDome(matrices, buffer, usedLight, radius * 1.1f, height * 0.5f, 2.2f, color);
            case "Sandogasa" -> buildDome(matrices, buffer, usedLight, radius, height, 1.0f, color);
            case "Halo" -> buildRing(matrices, buffer, usedLight, radius, radius * 0.75f, height, color);
            default -> buildDome(matrices, buffer, usedLight, radius, height, 1.8f, color);
        }

        if (module.trim.get() && !module.shape.is("Halo")) {
            buildRing(matrices, buffer, usedLight, radius * 1.04f, radius * 0.92f, 0.005f, module.trimColor.getArgb());
        }

        matrices.pop();
    }

    private boolean shouldRenderFor(KasaHatModule module, AbstractClientPlayerEntity entity) {
        boolean self = net.minecraft.client.MinecraftClient.getInstance().player == entity;
        return switch (module.targets.get()) {
            case "Только я" -> self;
            case "Кроме меня" -> !self;
            default -> true;
        };
    }

    /** Купол каса: кольца от широких полей к макушке. */
    private void buildDome(MatrixStack matrices, VertexConsumer buffer, int light,
                           float radius, float height, float curve, int color) {
        MatrixStack.Entry entry = matrices.peek();

        for (int ring = 0; ring < RINGS; ring++) {
            float t0 = ring / (float) RINGS;
            float t1 = (ring + 1) / (float) RINGS;

            float r0 = radius * (1f - t0);
            float r1 = radius * (1f - t1);
            float y0 = height * (float) Math.pow(t0, 1.0 / curve);
            float y1 = height * (float) Math.pow(t1, 1.0 / curve);

            for (int seg = 0; seg < SEGMENTS; seg++) {
                double a0 = Math.PI * 2 * seg / SEGMENTS;
                double a1 = Math.PI * 2 * (seg + 1) / SEGMENTS;

                float u0 = seg / (float) SEGMENTS;
                float u1 = (seg + 1) / (float) SEGMENTS;

                quad(entry, buffer, light, color,
                        cos(a0) * r0, y0, sin(a0) * r0, u0, t0,
                        cos(a1) * r0, y0, sin(a1) * r0, u1, t0,
                        cos(a1) * r1, y1, sin(a1) * r1, u1, t1,
                        cos(a0) * r1, y1, sin(a0) * r1, u0, t1);
            }
        }
    }

    /** Плоское кольцо (кант по краю полей или «нимб»). */
    private void buildRing(MatrixStack matrices, VertexConsumer buffer, int light,
                           float outer, float inner, float y, int color) {
        MatrixStack.Entry entry = matrices.peek();

        for (int seg = 0; seg < SEGMENTS; seg++) {
            double a0 = Math.PI * 2 * seg / SEGMENTS;
            double a1 = Math.PI * 2 * (seg + 1) / SEGMENTS;
            float u0 = seg / (float) SEGMENTS;
            float u1 = (seg + 1) / (float) SEGMENTS;

            quad(entry, buffer, light, color,
                    cos(a0) * outer, y, sin(a0) * outer, u0, 0f,
                    cos(a1) * outer, y, sin(a1) * outer, u1, 0f,
                    cos(a1) * inner, y, sin(a1) * inner, u1, 1f,
                    cos(a0) * inner, y, sin(a0) * inner, u0, 1f);
        }
    }

    private void quad(MatrixStack.Entry entry, VertexConsumer buffer, int light, int color,
                      float x1, float y1, float z1, float u1, float v1,
                      float x2, float y2, float z2, float u2, float v2,
                      float x3, float y3, float z3, float u3, float v3,
                      float x4, float y4, float z4, float u4, float v4) {
        vertex(entry, buffer, light, color, x1, y1, z1, u1, v1);
        vertex(entry, buffer, light, color, x2, y2, z2, u2, v2);
        vertex(entry, buffer, light, color, x3, y3, z3, u3, v3);
        vertex(entry, buffer, light, color, x4, y4, z4, u4, v4);
    }

    private void vertex(MatrixStack.Entry entry, VertexConsumer buffer, int light, int color,
                        float x, float y, float z, float u, float v) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        buffer.vertex(entry.getPositionMatrix(), x, y, z)
                .color(r, g, b, a)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry, 0f, 1f, 0f);
    }

    private static float cos(double angle) {
        return (float) Math.cos(angle);
    }

    private static float sin(double angle) {
        return (float) Math.sin(angle);
    }
}
