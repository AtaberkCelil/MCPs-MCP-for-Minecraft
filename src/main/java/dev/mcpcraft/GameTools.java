package dev.mcpcraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

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
        if (client == null || client.player == null || client.level == null) {
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
            case "break_block" -> breakBlock(arguments);
            case "place_block" -> placeBlock(arguments);
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
        state.addProperty("dimension", client.level.dimension().toString());
        return textResult("Player state", state.toString());
    }

    private static JsonObject nearbyBlocks(JsonObject arguments) {
        int radius = arguments.has("radius") ? Math.min(8, Math.max(1, arguments.get("radius").getAsInt())) : 2;
        BlockPos center = client.player.blockPosition();
        JsonArray blocks = new JsonArray();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos position = center.offset(x, y, z);
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
        return error("Screen inspection is not available through the 26.2 official client API yet.");
    }

    private static JsonObject pressKey(JsonObject arguments) {
        return error("Keyboard input is not available through the 26.2 official client API yet.");
    }

    private static JsonObject clickScreen(JsonObject arguments) {
        return error("Screen clicks are not available through the 26.2 official client API yet.");
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

    private static JsonObject breakBlock(JsonObject arguments) {
        if (!allowed(config == null || config.allowWorldActions)) return error("World actions are disabled in MCPs settings.");
        if (client.gameMode == null || !hasBlockPosition(arguments)) return error("x, y, and z are required.");
        BlockPos position = blockPosition(arguments);
        if (!withinActionDistance(position)) return error("Target block is too far away.");
        boolean broken = client.gameMode.destroyBlock(position);
        return textResult("Break block", broken ? "Broke the targeted block." : "The block could not be broken.");
    }

    private static JsonObject placeBlock(JsonObject arguments) {
        if (!allowed(config == null || config.allowWorldActions)) return error("World actions are disabled in MCPs settings.");
        if (client.gameMode == null || !hasBlockPosition(arguments)) return error("x, y, and z are required.");
        BlockPos position = blockPosition(arguments);
        if (!withinActionDistance(position)) return error("Target block is too far away.");
        Direction side = arguments.has("side") ? Direction.byName(arguments.get("side").getAsString()) : Direction.UP;
        if (side == null) side = Direction.UP;
        BlockPos support = position.relative(side.getOpposite());
        var hit = new net.minecraft.world.phys.BlockHitResult(
                new Vec3(support.getX() + 0.5, support.getY() + 0.5, support.getZ() + 0.5),
                side,
                support,
                false);
        var result = client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hit);
        return textResult("Place block", result.consumesAction() ? "Placed the held block." : "The held item could not place a block there.");
    }

    private static boolean hasBlockPosition(JsonObject arguments) {
        return arguments.has("x") && arguments.has("y") && arguments.has("z");
    }

    private static BlockPos blockPosition(JsonObject arguments) {
        return new BlockPos(arguments.get("x").getAsInt(), arguments.get("y").getAsInt(), arguments.get("z").getAsInt());
    }

    private static boolean withinActionDistance(BlockPos position) {
        int distance = config == null ? 6 : config.actionDistance;
        return client.player.blockPosition().distSqr(position) <= distance * distance;
    }

    private static JsonObject clickInventorySlot(JsonObject arguments) {
        return error("Inventory slot clicks are not available through the 26.2 official client API yet.");
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