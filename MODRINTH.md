# MCPs - MCP for Minecraft

Give your AI a live connection to Minecraft.

MCPs is a Fabric client-side mod that runs a local [Model Context Protocol](https://modelcontextprotocol.io/) server inside Minecraft. Connect an MCP-compatible AI client and let it read the current game state, inspect nearby blocks, send chat messages, and execute Minecraft commands from a controlled local endpoint.

## Features

- Read player position, health, hunger, and dimension
- Inspect nearby blocks around the player
- Send chat messages through the local player
- Execute Minecraft commands through the local player
- Localhost-only HTTP endpoint for reduced network exposure
- Designed for Fabric and Minecraft `26.2`

## Setup

1. Install Fabric Loader and Fabric API for Minecraft `26.2`.
2. Install the MCPs mod in the client `mods` folder.
3. Start Minecraft and enter a world.
4. Configure your MCP client with:

   ```text
   http://127.0.0.1:25575/mcp
   ```

The server starts when the client starts and stops when Minecraft closes.

## Security

MCPs binds only to `127.0.0.1`. The `execute_command` tool can perform powerful actions depending on the world and player permissions. Connect only AI clients you trust.

## Compatibility

- Minecraft: `26.2`
- Mod loader: Fabric
- Side: Client

## License

MCPs is licensed under the [MIT License](LICENSE).