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
 * Шляпа над головой игрока. Каркасный режим рисует линиями (меридианы + кольца),
 * режим «Каркас+сетка» добавляет решётку в плоскости полей, «Заливка» — сплошные грани.
 */
public class KasaFeatureRenderer
        extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    private static final Identifier TEXTURE = Identifier.of("wanted", "textures/entity/kasa.png");
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
        float curve = curveFor(module.shape.get());
        int segments = module.segments.getInt();

        if (module.style.is("Заливка")) {
            int usedLight = module.glow.get() ? LightmapTextureManager.MAX_LIGHT_COORDINATE : light;
            VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));
            if (module.shape.is("Кольцо")) {
                buildSolidRing(matrices, buffer, usedLight, segments, radius, radius * 0.75f, height,
                        module.color.getArgb());
            } else {
                buildSolidDome(matrices, buffer, usedLight, segments, radius, height, curve,
                        module.color.getArgb());
            }
            if (module.trim.get() && !module.shape.is("Кольцо")) {
                buildSolidRing(matrices, buffer, usedLight, segments,
                        radius * 1.04f, radius * 0.92f, 0.005f, module.trimColor.getArgb());
            }
        } else {
            VertexConsumer lines = vertexConsumers.getBuffer(RenderLayer.getLines());
            buildWireDome(matrices, lines, segments, radius, height, curve, module.color.getArgb(),
                    module.shape.is("Кольцо"));
            if (module.style.is("Каркас+сетка")) {
                buildGrid(matrices, lines, radius, module.gridColor.getArgb());
            }
            if (module.trim.get()) {
                buildWireCircle(matrices, lines, segments, radius * 1.04f, 0.005f, module.trimColor.getArgb());
            }
        }

        matrices.pop();
    }

    private static float curveFor(String shape) {
        return switch (shape) {
            case "Плоская" -> 2.6f;
            case "Купол" -> 1.0f;
            default -> 1.8f;
        };
    }

    private boolean shouldRenderFor(KasaHatModule module, AbstractClientPlayerEntity entity) {
        boolean self = net.minecraft.client.MinecraftClient.getInstance().player == entity;
        return switch (module.targets.get()) {
            case "Только я" -> self;
            case "Кроме меня" -> !self;
            default -> true;
        };
    }

    // ------------------------------------------------------------------ каркас

    /** Меридианы от края полей к макушке плюс горизонтальные кольца. */
    private void buildWireDome(MatrixStack matrices, VertexConsumer buffer, int segments,
                               float radius, float height, float curve, int color, boolean ringOnly) {
        MatrixStack.Entry entry = matrices.peek();

        if (!ringOnly) {
            for (int seg = 0; seg < segments; seg++) {
                double angle = Math.PI * 2 * seg / segments;
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                float previousX = cos * radius;
                float previousY = 0f;
                float previousZ = sin * radius;

                for (int ring = 1; ring <= RINGS; ring++) {
                    float t = ring / (float) RINGS;
                    float r = radius * (1f - t);
                    float y = height * (float) Math.pow(t, 1.0 / curve);
                    line(entry, buffer, color, previousX, previousY, previousZ, cos * r, y, sin * r);
                    previousX = cos * r;
                    previousY = y;
                    previousZ = sin * r;
                }
            }
        }

        int ringCount = ringOnly ? 1 : RINGS;
        for (int ring = 0; ring < ringCount; ring++) {
            float t = ring / (float) RINGS;
            float r = radius * (1f - t);
            float y = height * (float) Math.pow(t, 1.0 / curve);
            buildWireCircle(matrices, buffer, segments, r, y, color);
        }
    }

    private void buildWireCircle(MatrixStack matrices, VertexConsumer buffer, int segments,
                                 float radius, float y, int color) {
        MatrixStack.Entry entry = matrices.peek();
        for (int seg = 0; seg < segments; seg++) {
            double a0 = Math.PI * 2 * seg / segments;
            double a1 = Math.PI * 2 * (seg + 1) / segments;
            line(entry, buffer, color,
                    (float) Math.cos(a0) * radius, y, (float) Math.sin(a0) * radius,
                    (float) Math.cos(a1) * radius, y, (float) Math.sin(a1) * radius);
        }
    }

    /** Плоская решётка в плоскости полей, обрезанная по кругу. */
    private void buildGrid(MatrixStack matrices, VertexConsumer buffer, float radius, int color) {
        MatrixStack.Entry entry = matrices.peek();
        int cells = 8;
        float step = radius * 2f / cells;

        for (int i = 0; i <= cells; i++) {
            float offset = -radius + step * i;
            float half = (float) Math.sqrt(Math.max(0f, radius * radius - offset * offset));
            if (half <= 0.001f) continue;
            line(entry, buffer, color, offset, 0f, -half, offset, 0f, half);
            line(entry, buffer, color, -half, 0f, offset, half, 0f, offset);
        }
    }

    private void line(MatrixStack.Entry entry, VertexConsumer buffer, int color,
                      float x1, float y1, float z1, float x2, float y2, float z2) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        float nx = x2 - x1;
        float ny = y2 - y1;
        float nz = z2 - z1;
        float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length < 1.0E-5f) return;
        nx /= length;
        ny /= length;
        nz /= length;

        buffer.vertex(entry.getPositionMatrix(), x1, y1, z1).color(r, g, b, a).normal(entry, nx, ny, nz);
        buffer.vertex(entry.getPositionMatrix(), x2, y2, z2).color(r, g, b, a).normal(entry, nx, ny, nz);
    }

    // ---------------------------------------------------------------- заливка

    private void buildSolidDome(MatrixStack matrices, VertexConsumer buffer, int light, int segments,
                                float radius, float height, float curve, int color) {
        MatrixStack.Entry entry = matrices.peek();

        for (int ring = 0; ring < RINGS; ring++) {
            float t0 = ring / (float) RINGS;
            float t1 = (ring + 1) / (float) RINGS;

            float r0 = radius * (1f - t0);
            float r1 = radius * (1f - t1);
            float y0 = height * (float) Math.pow(t0, 1.0 / curve);
            float y1 = height * (float) Math.pow(t1, 1.0 / curve);

            for (int seg = 0; seg < segments; seg++) {
                double a0 = Math.PI * 2 * seg / segments;
                double a1 = Math.PI * 2 * (seg + 1) / segments;
                float u0 = seg / (float) segments;
                float u1 = (seg + 1) / (float) segments;

                quad(entry, buffer, light, color,
                        cos(a0) * r0, y0, sin(a0) * r0, u0, t0,
                        cos(a1) * r0, y0, sin(a1) * r0, u1, t0,
                        cos(a1) * r1, y1, sin(a1) * r1, u1, t1,
                        cos(a0) * r1, y1, sin(a0) * r1, u0, t1);
            }
        }
    }

    private void buildSolidRing(MatrixStack matrices, VertexConsumer buffer, int light, int segments,
                                float outer, float inner, float y, int color) {
        MatrixStack.Entry entry = matrices.peek();

        for (int seg = 0; seg < segments; seg++) {
            double a0 = Math.PI * 2 * seg / segments;
            double a1 = Math.PI * 2 * (seg + 1) / segments;
            float u0 = seg / (float) segments;
            float u1 = (seg + 1) / (float) segments;

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
