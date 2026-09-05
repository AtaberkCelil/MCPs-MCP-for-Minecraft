package dev.mcpcraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public final class McpCraftClient implements ClientModInitializer {
    private static final int DEFAULT_PORT = 25575;
    private McpHttpServer server;

    @Override
    public void onInitializeClient() {
        GameTools.bind(Minecraft.getInstance());
        server = new McpHttpServer(DEFAULT_PORT, this::runOnClientThread);
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> server.start());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> server.stop());
        ClientTickEvents.END_CLIENT_TICK.register(client -> server.drainMainThreadTasks());
    }

    private void runOnClientThread(Runnable task) {
        Minecraft client = Minecraft.getInstance();
        if (client.isSameThread()) {
            task.run();
        } else {
            client.execute(task);
        }
    }
}