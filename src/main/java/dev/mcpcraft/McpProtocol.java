package dev.mcpcraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.concurrent.CompletableFuture;

public final class McpProtocol {
    private McpProtocol() {}

    public static CompletableFuture<JsonObject> handle(JsonObject request, GameDispatcher dispatcher) {
        String method = request.has("method") ? request.get("method").getAsString() : "";
        JsonElement id = request.get("id");
        return switch (method) {
            case "initialize" -> CompletableFuture.completedFuture(result(id, initializeResult()));
            case "notifications/initialized" -> CompletableFuture.completedFuture(result(id, new JsonObject()));
            case "tools/list" -> CompletableFuture.completedFuture(result(id, tools()));
            case "tools/call" -> callTool(id, request.getAsJsonObject("params"), dispatcher);
            default -> CompletableFuture.completedFuture(error(id, -32601, "Unknown MCP method: " + method));
        };
    }

    private static JsonObject initializeResult() {
        JsonObject result = new JsonObject();
        result.addProperty("protocolVersion", "2025-06-18");
        JsonObject capabilities = new JsonObject();
        capabilities.add("tools", new JsonObject());
        result.add("capabilities", capabilities);
        JsonObject serverInfo = new JsonObject();
        serverInfo.addProperty("name", "mcps");
        serverInfo.addProperty("version", "0.1.0");
        result.add("serverInfo", serverInfo);
        return result;
    }

    private static JsonObject tools() {
        JsonArray tools = new JsonArray();
        tools.add(tool("get_player_state", "Read the local player's position, health, hunger, and dimension.", new JsonObject()));
        tools.add(tool("get_nearby_blocks", "Read blocks in a small radius around the local player.", schema("radius", "integer", 1, 8)));
        tools.add(tool("get_screen_state", "Read the currently open Minecraft screen so the AI can navigate menus.", new JsonObject()));
        tools.add(tool("press_key", "Send a key press to the currently open Minecraft screen.", objectSchema("keyCode", "keyCode", "integer", "GLFW key code.", "scanCode", "integer", "Optional scan code.", "modifiers", "integer", "Optional GLFW modifiers.")));
        tools.add(tool("click_screen", "Click a coordinate in the currently open Minecraft screen.", objectSchema("x", "x", "number", "Screen x coordinate.", "y", "y", "number", "Screen y coordinate.", "button", "integer", "Mouse button, default 0.")));
        tools.add(tool("move_player", "Apply a short movement impulse relative to the player's facing direction.", objectSchema(null, "forward", "number", "Forward/backward input.", "strafe", "number", "Left/right input.", "jump", "boolean", "Whether to jump.")));
        tools.add(tool("look", "Set the local player's yaw and pitch.", objectSchema(null, "yaw", "number", "Yaw in degrees.", "pitch", "number", "Pitch in degrees.")));
        tools.add(tool("attack", "Attack the entity currently under the crosshair.", new JsonObject()));
        tools.add(tool("use_item", "Use the item in the main hand.", new JsonObject()));
        tools.add(tool("click_inventory_slot", "Click a slot in the currently open inventory screen.", objectSchema("slot", "slot", "integer", "Slot id.", "button", "integer", "Mouse button, default 0.")));
        tools.add(tool("select_hotbar_slot", "Select a hotbar slot from 0 through 8.", schema("slot", "integer", 0, 8)));
        tools.add(tool("send_chat", "Send a chat message as the local player.", schema("message", "string", null, null)));
        tools.add(tool("execute_command", "Run a Minecraft command without the leading slash.", schema("command", "string", null, null)));
        JsonObject result = new JsonObject();
        result.add("tools", tools);
        return result;
    }

    private static JsonObject tool(String name, String description, JsonObject inputSchema) {
        JsonObject tool = new JsonObject();
        tool.addProperty("name", name);
        tool.addProperty("description", description);
        tool.add("inputSchema", inputSchema);
        return tool;
    }

    private static JsonObject schema(String property, String type, Integer minimum, Integer maximum) {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        JsonObject properties = new JsonObject();
        JsonObject value = new JsonObject();
        value.addProperty("type", type);
        if (minimum != null) value.addProperty("minimum", minimum);
        if (maximum != null) value.addProperty("maximum", maximum);
        properties.add(property, value);
        schema.add("properties", properties);
        JsonArray required = new JsonArray();
        required.add(property);
        schema.add("required", required);
        return schema;
    }

    private static JsonObject objectSchema(String required, String... fields) {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        JsonObject properties = new JsonObject();
        for (int index = 0; index < fields.length; index += 4) {
            JsonObject property = new JsonObject();
            property.addProperty("type", fields[index + 2]);
            property.addProperty("description", fields[index + 3]);
            properties.add(fields[index + 1], property);
        }
        schema.add("properties", properties);
        if (required != null) {
            JsonArray requiredProperties = new JsonArray();
            requiredProperties.add(required);
            schema.add("required", requiredProperties);
        }
        return schema;
    }

    private static CompletableFuture<JsonObject> callTool(JsonElement id, JsonObject params, GameDispatcher dispatcher) {
        if (params == null || !params.has("name")) return CompletableFuture.completedFuture(error(id, -32602, "tools/call requires params.name"));
        String name = params.get("name").getAsString();
        JsonObject arguments = params.has("arguments") ? params.getAsJsonObject("arguments") : new JsonObject();
        return dispatcher.dispatch(name, arguments).thenApply(result -> result(id, result));
    }

    private static JsonObject result(JsonElement id, JsonObject value) {
        JsonObject response = new JsonObject();
        if (id != null) response.add("id", id);
        response.add("result", value);
        response.addProperty("jsonrpc", "2.0");
        return response;
    }

    public static JsonObject error(JsonElement id, int code, String message) {
        JsonObject response = new JsonObject();
        if (id != null) response.add("id", id);
        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message == null ? "Unknown error" : message);
        response.add("error", error);
        response.addProperty("jsonrpc", "2.0");
        return response;
    }

    @FunctionalInterface
    public interface GameDispatcher {
        CompletableFuture<JsonObject> dispatch(String name, JsonObject arguments);
    }
}