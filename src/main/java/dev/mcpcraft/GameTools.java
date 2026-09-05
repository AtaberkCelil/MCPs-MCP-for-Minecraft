package dev.mcpcraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.inventory.ClickType;

public final class GameTools {
    private static Minecraft client;
    private static McpsConfig config;

    private GameTools() {}

    public static void bind(Minecraft minecraftClient) {
        client = minecraftClient;
    }

    public static void bindConfig(McpsConfig mcpsConfig) {
        config = mcpsConfig;
    }

    public static JsonObject dispatch(String name, JsonObject arguments) {
        if (client == null || client.player == null || client.world == null) {
            return error("A world and local player must be available.");
        }
        return switch (name) {
            case "get_player_state" -> playerState();
            case "get_nearby_blocks" -> nearbyBlocks(arguments);
            case "get_screen_state" -> screenState();
            case "press_key" -> pressKey(arguments);
            case "click_screen" -> clickScreen(arguments);
            case "move_player" -> movePlayer(arguments);
            case "look" -> look(arguments);
            case "attack" -> attack();
            case "use_item" -> useItem();
            case "click_inventory_slot" -> clickInventorySlot(arguments);
            case "select_hotbar_slot" -> selectHotbarSlot(arguments);
            case "send_chat" -> sendChat(arguments);
            case "execute_command" -> executeCommand(arguments);
            default -> error("Unknown tool: " + name);
        };
    }

    private static JsonObject playerState() {
        JsonObject state = new JsonObject();
        state.addProperty("name", client.player.getName().getString());
        state.addProperty("x", client.player.getX());
        state.addProperty("y", client.player.getY());
        state.addProperty("z", client.player.getZ());
        state.addProperty("health", client.player.getHealth());
        state.addProperty("food", client.player.getFoodData().getFoodLevel());
        state.addProperty("dimension", client.level.dimension().location().toString());
        return textResult("Player state", state.toString());
    }

    private static JsonObject nearbyBlocks(JsonObject arguments) {
        int radius = arguments.has("radius") ? Math.min(8, Math.max(1, arguments.get("radius").getAsInt())) : 2;
        BlockPos center = client.player.getBlockPos();
        JsonArray blocks = new JsonArray();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos position = center.add(x, y, z);
                    JsonObject block = new JsonObject();
                    block.addProperty("x", position.getX());
                    block.addProperty("y", position.getY());
                    block.addProperty("z", position.getZ());
                    block.addProperty("id", client.level.getBlockState(position).getBlock().toString());
                    blocks.add(block);
                }
            }
        }
        return textResult("Nearby blocks", blocks.toString());
    }

    private static JsonObject sendChat(JsonObject arguments) {
        if (!allowed(config == null || config.allowChat)) return error("Chat actions are disabled in MCPs settings.");
        if (!arguments.has("message")) return error("message is required");
        client.player.connection.sendChat(arguments.get("message").getAsString());
        return textResult("Chat", "Message sent.");
    }

    private static JsonObject executeCommand(JsonObject arguments) {
        if (!allowed(config == null || config.allowCommands)) return error("Command actions are disabled in MCPs settings.");
        if (!arguments.has("command")) return error("command is required");
        String command = arguments.get("command").getAsString().trim();
        if (command.startsWith("/")) command = command.substring(1);
        client.player.connection.sendCommand(command);
        return textResult("Command", "Command sent.");
    }

    private static JsonObject screenState() {
        if (client.screen == null) return textResult("Screen", "No screen is open.");
        return textResult("Screen", client.screen.getClass().getName() + " (" + client.screen.width + "x" + client.screen.height + ")");
    }

    private static JsonObject pressKey(JsonObject arguments) {
        if (!allowed(config == null || config.allowInput)) return error("Keyboard input is disabled in MCPs settings.");
        if (client.screen == null || !arguments.has("keyCode")) return error("An open screen and keyCode are required.");
        int keyCode = arguments.get("keyCode").getAsInt();
        int scanCode = arguments.has("scanCode") ? arguments.get("scanCode").getAsInt() : 0;
        int modifiers = arguments.has("modifiers") ? arguments.get("modifiers").getAsInt() : 0;
        boolean handled = client.screen.keyPressed(keyCode, scanCode, modifiers);
        return textResult("Key", handled ? "Key handled by screen." : "Key was not handled.");
    }

    private static JsonObject clickScreen(JsonObject arguments) {
        if (!allowed(config == null || config.allowInput)) return error("Mouse input is disabled in MCPs settings.");
        if (client.screen == null || !arguments.has("x") || !arguments.has("y")) return error("An open screen and x/y are required.");
        double x = arguments.get("x").getAsDouble();
        double y = arguments.get("y").getAsDouble();
        int button = arguments.has("button") ? arguments.get("button").getAsInt() : 0;
        boolean handled = client.screen.mouseClicked(x, y, button);
        return textResult("Mouse", handled ? "Click handled by screen." : "Click was not handled.");
    }

    private static JsonObject movePlayer(JsonObject arguments) {
        if (!allowed(config == null || config.allowWorldActions)) return error("World actions are disabled in MCPs settings.");
        double forward = arguments.has("forward") ? arguments.get("forward").getAsDouble() : 0;
        double strafe = arguments.has("strafe") ? arguments.get("strafe").getAsDouble() : 0;
        Vec3 movement = new Vec3(strafe, arguments.has("jump") && arguments.get("jump").getAsBoolean() ? 0.42 : 0, forward)
                .yRot(-client.player.getYRot() * ((float) Math.PI / 180F));
        client.player.setDeltaMovement(movement.scale(0.15));
        return textResult("Movement", "Applied movement input.");
    }

    private static JsonObject look(JsonObject arguments) {
        if (!allowed(config == null || config.allowWorldActions)) return error("World actions are disabled in MCPs settings.");
        if (arguments.has("yaw")) client.player.setYRot(arguments.get("yaw").getAsFloat());
        if (arguments.has("pitch")) client.player.setXRot(Math.max(-90, Math.min(90, arguments.get("pitch").getAsFloat())));
        return textResult("Look", "Updated player look direction.");
    }

    private static JsonObject attack() {
        if (!allowed(config == null || config.allowWorldActions)) return error("World actions are disabled in MCPs settings.");
        if (client.gameMode == null || client.crosshairPickEntity == null) return error("No entity is under the crosshair.");
        client.gameMode.attack(client.player, client.crosshairPickEntity);
        return textResult("Attack", "Attacked the targeted entity.");
    }

    private static JsonObject useItem() {
        if (!allowed(config == null || config.allowWorldActions)) return error("World actions are disabled in MCPs settings.");
        if (client.gameMode == null) return error("No game mode is available.");
        client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
        return textResult("Use", "Used the item in the main hand.");
    }

    private static JsonObject clickInventorySlot(JsonObject arguments) {
        if (!allowed(config == null || config.allowInput)) return error("Inventory input is disabled in MCPs settings.");
        if (client.gameMode == null || client.player.containerMenu == null || !arguments.has("slot")) return error("An open inventory and slot are required.");
        int slot = arguments.get("slot").getAsInt();
        int button = arguments.has("button") ? arguments.get("button").getAsInt() : 0;
        client.gameMode.handleInventoryMouseClick(client.player.containerMenu.containerId, slot, button, ClickType.PICKUP, client.player);
        return textResult("Inventory", "Clicked inventory slot " + slot + ".");
    }

    private static JsonObject selectHotbarSlot(JsonObject arguments) {
        if (!allowed(config == null || config.allowInput)) return error("Inventory input is disabled in MCPs settings.");
        if (!arguments.has("slot")) return error("slot is required");
        int slot = Math.max(0, Math.min(8, arguments.get("slot").getAsInt()));
        client.player.getInventory().setSelectedSlot(slot);
        return textResult("Hotbar", "Selected hotbar slot " + slot + ".");
    }

    private static boolean allowed(boolean value) {
        return value;
    }

    private static JsonObject textResult(String title, String text) {
        JsonObject result = new JsonObject();
        JsonArray content = new JsonArray();
        JsonObject item = new JsonObject();
        item.addProperty("type", "text");
        item.addProperty("text", text);
        content.add(item);
        result.add("content", content);
        result.addProperty("isError", false);
        return result;
    }

    private static JsonObject error(String message) {
        JsonObject result = textResult("Error", message);
        result.addProperty("isError", true);
        return result;
    }
}