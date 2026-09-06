package wanted.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import wanted.WantedClient;
import wanted.module.Module;
import wanted.module.ModuleManager;
import wanted.setting.BooleanSetting;
import wanted.setting.ColorSetting;
import wanted.setting.ModeSetting;
import wanted.setting.NumberSetting;
import wanted.setting.Setting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Сохранение и загрузка состояния модулей в config/wanted.json. */
public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigManager() {
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("wanted.json");
    }

    public static void save() {
        JsonObject root = new JsonObject();
        root.addProperty("customMainMenu", WantedClient.customMainMenu);

        JsonObject modules = new JsonObject();
        for (Module module : ModuleManager.getModules()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("enabled", module.isEnabled());
            entry.addProperty("key", module.getKeyCode());

            JsonObject settings = new JsonObject();
            for (Setting setting : module.getSettings()) {
                if (setting instanceof BooleanSetting bool) {
                    settings.addProperty(setting.getName(), bool.get());
                } else if (setting instanceof NumberSetting number) {
                    settings.addProperty(setting.getName(), number.get());
                } else if (setting instanceof ModeSetting mode) {
                    settings.addProperty(setting.getName(), mode.get());
                } else if (setting instanceof ColorSetting color) {
                    JsonObject colorObject = new JsonObject();
                    colorObject.addProperty("hue", color.getHue());
                    colorObject.addProperty("saturation", color.getSaturation());
                    colorObject.addProperty("brightness", color.getBrightness());
                    colorObject.addProperty("alpha", color.getAlpha());
                    colorObject.addProperty("rainbow", color.isRainbow());
                    settings.add(setting.getName(), colorObject);
                }
            }
            entry.add("settings", settings);
            modules.add(module.getName(), entry);
        }
        root.add("modules", modules);

        try {
            Files.createDirectories(file().getParent());
            Files.writeString(file(), GSON.toJson(root));
        } catch (IOException exception) {
            WantedClient.LOGGER.warn("Не удалось сохранить конфиг", exception);
        }
    }

    public static void load() {
        Path path = file();
        if (!Files.exists(path)) return;

        try {
            JsonElement parsed = JsonParser.parseString(Files.readString(path));
            if (!parsed.isJsonObject()) return;
            JsonObject root = parsed.getAsJsonObject();

            if (root.has("customMainMenu")) {
                WantedClient.customMainMenu = root.get("customMainMenu").getAsBoolean();
            }
            if (!root.has("modules")) return;

            JsonObject modules = root.getAsJsonObject("modules");
            for (Module module : ModuleManager.getModules()) {
                if (!modules.has(module.getName())) continue;
                JsonObject entry = modules.getAsJsonObject(module.getName());

                if (entry.has("key")) module.setKeyCode(entry.get("key").getAsInt());
                if (entry.has("settings")) loadSettings(module, entry.getAsJsonObject("settings"));
                if (entry.has("enabled")) module.setEnabled(entry.get("enabled").getAsBoolean());
            }
        } catch (Exception exception) {
            WantedClient.LOGGER.warn("Не удалось прочитать конфиг", exception);
        }
    }

    private static void loadSettings(Module module, JsonObject settings) {
        for (Setting setting : module.getSettings()) {
            if (!settings.has(setting.getName())) continue;
            JsonElement value = settings.get(setting.getName());

            if (setting instanceof BooleanSetting bool) {
                bool.set(value.getAsBoolean());
            } else if (setting instanceof NumberSetting number) {
                number.set(value.getAsDouble());
            } else if (setting instanceof ModeSetting mode) {
                mode.set(value.getAsString());
            } else if (setting instanceof ColorSetting color && value.isJsonObject()) {
                JsonObject colorObject = value.getAsJsonObject();
                color.setHue(colorObject.get("hue").getAsFloat());
                color.setSaturation(colorObject.get("saturation").getAsFloat());
                color.setBrightness(colorObject.get("brightness").getAsFloat());
                color.setAlpha(colorObject.get("alpha").getAsFloat());
                color.setRainbow(colorObject.get("rainbow").getAsBoolean());
            }
        }
    }
}
