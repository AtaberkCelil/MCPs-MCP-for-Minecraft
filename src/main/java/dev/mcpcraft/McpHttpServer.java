package dev.mcpcraft;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

public final class McpHttpServer {
    private static final Gson GSON = new Gson();
    private final int port;
    private final ConcurrentLinkedQueue<Runnable> mainThreadTasks = new ConcurrentLinkedQueue<>();
    private HttpServer server;

    public McpHttpServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
            server.createContext("/mcp", this::handleRequest);
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            server.start();
            System.out.println("[MCPs] MCP endpoint listening at http://127.0.0.1:" + port + "/mcp");
        } catch (IOException exception) {
            System.err.println("[MCPs] Could not start MCP endpoint: " + exception.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public void drainMainThreadTasks() {
        Runnable task;
        while ((task = mainThreadTasks.poll()) != null) {
            task.run();
        }
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        try (InputStream body = exchange.getRequestBody()) {
            JsonObject request = GSON.fromJson(new String(body.readAllBytes(), StandardCharsets.UTF_8), JsonObject.class);
            JsonObject response = McpProtocol.handle(request, this::dispatchToGame).join();
            byte[] payload = GSON.toJson(response).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, payload.length);
            exchange.getResponseBody().write(payload);
        } catch (Exception exception) {
            byte[] payload = GSON.toJson(McpProtocol.error(null, -32603, exception.getMessage())).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, payload.length);
            exchange.getResponseBody().write(payload);
        } finally {
            exchange.close();
        }
    }

    private CompletableFuture<JsonObject> dispatchToGame(String name, JsonObject arguments) {
        CompletableFuture<JsonObject> result = new CompletableFuture<>();
        mainThreadTasks.add(() -> result.complete(GameTools.dispatch(name, arguments)));
        return result;
    }
}