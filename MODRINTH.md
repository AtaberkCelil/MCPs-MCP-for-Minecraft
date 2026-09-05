# MCPs - MCP for Minecraft

Give your AI a live connection to Minecraft.

MCPs is a Fabric client-side mod that runs a local [Model Context Protocol](https://modelcontextprotocol.io/) server inside Minecraft. It gives an MCP-compatible AI client tools to read the player and world, inspect nearby blocks, navigate menus, click inventory slots, move, look, attack, use items, send chat, and execute commands.

Install MCPs if you want to control and observe a Minecraft session from an AI assistant through the same kinds of interactions available to a player. This makes MCPs useful for automation, accessibility, testing, guided building, and AI-assisted exploration without requiring a separate bridge application.

Before downloading, know that MCPs is a client-side mod for Fabric and requires Minecraft `26.2`, Fabric Loader, Fabric API, and Java `25` or newer. An MCP client must connect to the local endpoint, and powerful actions such as commands and world interaction can be enabled or disabled in the in-game settings. The server binds to `127.0.0.1` by default, but you should still connect only trusted AI clients.

## Features

- Read player position, health, hunger, and dimension
- Inspect nearby blocks around the player
- Send chat messages through the local player
- Execute Minecraft commands through the local player
- Navigate screens, click buttons, use inventories, and select hotbar slots
- Move, look, attack, and use the held item
- Break and place blocks through normal player interaction APIs
- Sprint, crouch, jump, and eat through player-like input
- Inspect nearby entities and non-empty inventory slots
- Scan a larger area of blocks for better AI awareness
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

Install Mod Menu to open **MCPs Settings** in-game. The Cloth Config screen controls the port, automatic startup, chat, commands, input, inventory, and world-action permissions.

## Security

MCPs binds only to `127.0.0.1`. The `execute_command` tool can perform powerful actions depending on the world and player permissions. Connect only AI clients you trust.

## Compatibility

- Minecraft: `26.2`
- Mod loader: Fabric
- Side: Client

## License

MCPs is licensed under the [MIT License](LICENSE).