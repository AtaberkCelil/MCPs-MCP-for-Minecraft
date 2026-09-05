package dev.mcpcraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

public final class GameTools {
    private static Minecraft client;

    private GameTools() {}

    public static void bind(Minecraft minecraftClient) {
        client = minecraftClient;
    }

    public static JsonObject dispatch(String name, JsonObject arguments) {
        if (client == null || client.player == null || client.world == null) {
            return error("A world and local player must be available.");
        }
        return switch (name) {
            case "get_player_state" -> playerState();
            case "get_nearby_blocks" -> nearbyBlocks(arguments);
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
        if (!arguments.has("message")) return error("message is required");
        client.player.connection.sendChat(arguments.get("message").getAsString());
        return textResult("Chat", "Message sent.");
    }

    private static JsonObject executeCommand(JsonObject arguments) {
        if (!arguments.has("command")) return error("command is required");
        String command = arguments.get("command").getAsString().trim();
        if (command.startsWith("/")) command = command.substring(1);
        client.player.connection.sendCommand(command);
        return textResult("Command", "Command sent.");
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