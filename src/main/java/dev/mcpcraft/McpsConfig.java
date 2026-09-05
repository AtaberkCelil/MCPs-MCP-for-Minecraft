package dev.mcpcraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class McpsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("mcps.json");

    public int port = 25575;
    public boolean startServer = true;
    public boolean allowChat = true;
    public boolean allowCommands = true;
    public boolean allowInput = true;
    public boolean allowWorldActions = true;
    public int actionDistance = 6;

    public static McpsConfig load() {
        if (!Files.exists(PATH)) {
            McpsConfig config = new McpsConfig();
            config.save();
            return config;
        }
        try {
            McpsConfig config = GSON.fromJson(Files.readString(PATH, StandardCharsets.UTF_8), McpsConfig.class);
            if (config == null) config = new McpsConfig();
            config.clamp();
            return config;
        } catch (Exception exception) {
            System.err.println("[MCPs] Could not read config: " + exception.getMessage());
            return new McpsConfig();
        }
    }

    public void save() {
        clamp();
        try {
            Files.writeString(PATH, GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            System.err.println("[MCPs] Could not save config: " + exception.getMessage());
        }
    }

    private void clamp() {
        port = Math.max(1024, Math.min(65535, port));
        actionDistance = Math.max(1, Math.min(32, actionDistance));
    }
}