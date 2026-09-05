package dev.mcpcraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public final class McpCraftClient implements ClientModInitializer {
    private static McpsConfig config;
    private McpHttpServer server;

    @Override
    public void onInitializeClient() {
        McpCraftClientHolder.INSTANCE = this;
        config = McpsConfig.load();
        GameTools.bind(Minecraft.getInstance());
        GameTools.bindConfig(config);
        BlockBreakingManager.bind(Minecraft.getInstance());
        server = new McpHttpServer(config.port);
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            if (config.startServer) server.start();
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> server.stop());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            BlockBreakingManager.tick();
            server.drainMainThreadTasks();
        });
    }

    public static McpsConfig config() {
        return config;
    }

    public static void saveConfig() {
        config.save();
        GameTools.bindConfig(config);
        if (Minecraft.getInstance().level != null) {
            reloadServer();
        }
    }

    private static void reloadServer() {
        // The server instance is recreated so a changed port applies immediately.
        McpCraftClient instance = McpCraftClientHolder.INSTANCE;
        if (instance.server != null) instance.server.stop();
        instance.server = new McpHttpServer(config.port);
        if (config.startServer) instance.server.start();
    }

    private static final class McpCraftClientHolder {
        private static McpCraftClient INSTANCE;
    }
}