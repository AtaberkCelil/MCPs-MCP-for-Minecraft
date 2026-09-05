package dev.mcpcraft;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class McpsConfigScreen {
    private McpsConfigScreen() {}

    public static Screen create(Screen parent) {
        McpsConfig config = McpCraftClient.config();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title.mcps.config"))
                .setSavingRunnable(McpCraftClient::saveConfig);

        builder.getOrCreateCategory(Component.translatable("category.mcps.server"))
                .addEntry(builder.entryBuilder()
                        .startIntField(Component.translatable("option.mcps.port"), config.port)
                        .setDefaultValue(25575)
                        .setMin(1024)
                        .setMax(65535)
                        .setSaveConsumer(value -> config.port = value)
                        .build())
                .addEntry(builder.entryBuilder()
                        .startBooleanToggle(Component.translatable("option.mcps.start_server"), config.startServer)
                        .setDefaultValue(true)
                        .setSaveConsumer(value -> config.startServer = value)
                        .build());

        builder.getOrCreateCategory(Component.translatable("category.mcps.permissions"))
                .addEntry(builder.entryBuilder()
                        .startBooleanToggle(Component.translatable("option.mcps.allow_chat"), config.allowChat)
                        .setDefaultValue(true)
                        .setSaveConsumer(value -> config.allowChat = value)
                        .build())
                .addEntry(builder.entryBuilder()
                        .startBooleanToggle(Component.translatable("option.mcps.allow_commands"), config.allowCommands)
                        .setDefaultValue(true)
                        .setSaveConsumer(value -> config.allowCommands = value)
                        .build())
                .addEntry(builder.entryBuilder()
                        .startBooleanToggle(Component.translatable("option.mcps.allow_input"), config.allowInput)
                        .setDefaultValue(true)
                        .setSaveConsumer(value -> config.allowInput = value)
                        .build())
                .addEntry(builder.entryBuilder()
                        .startBooleanToggle(Component.translatable("option.mcps.allow_world_actions"), config.allowWorldActions)
                        .setDefaultValue(true)
                        .setSaveConsumer(value -> config.allowWorldActions = value)
                        .build())
                .addEntry(builder.entryBuilder()
                        .startIntField(Component.translatable("option.mcps.action_distance"), config.actionDistance)
                        .setDefaultValue(6)
                        .setMin(1)
                        .setMax(32)
                        .setSaveConsumer(value -> config.actionDistance = value)
                        .build());

        return builder.build();
    }
}