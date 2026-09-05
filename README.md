# MCPs - MCP for Minecraft

<p align="center">
	<img src="logo.png" alt="MCPs logo" width="128" height="128">
</p>

MCPs is a Fabric client mod that exposes a local Model Context Protocol endpoint so an MCP-capable AI client can inspect and control a Minecraft session.

Install it to connect an AI assistant directly to Minecraft for automation, accessibility, testing, guided building, and AI-assisted exploration. MCPs is client-side, requires Fabric on Minecraft `26.2` with Java `25+`, and should only be connected to AI clients you trust because it can perform player actions and commands.

## Status

The project targets Minecraft `26.2` and Fabric Loader `0.19.5`. The initial scaffold exposes the protocol endpoint at `http://127.0.0.1:25575/mcp`.

## Build

Install Java 25 and Gradle 9.5 or newer, then run:

```powershell
.\gradlew.bat build
```

The generated mod jar will be in `build/libs`.

> Note: Fabric currently lists Minecraft `26.2`, but its Yarn and official Mojang mapping artifacts are not published yet. The project keeps the requested `26.2` target and will build once those upstream mappings are available.

## Connecting an MCP client

Add the endpoint as a Streamable HTTP MCP server using:

```text
http://127.0.0.1:25575/mcp
```

The endpoint binds to loopback only. Do not expose it directly to a network without adding authentication and an explicit permission model.

## Configuration

Install Mod Menu to open **MCPs Settings** from the Mods screen. Cloth Config provides the settings screen. You can change the port, enable or disable automatic startup, and independently allow chat, commands, keyboard and mouse input, inventory input, and world actions. Changes to the port restart the local endpoint immediately.

## Available tools

- `get_player_state`
- `get_nearby_blocks`
- `get_screen_state`
- `press_key`
- `click_screen`
- `move_player`
- `look`
- `attack`
- `use_item`
- `click_inventory_slot`
- `select_hotbar_slot`
- `send_chat`
- `execute_command`

The screen and inventory tools allow an AI client to navigate ordinary Minecraft menus and crafting interfaces by observing the current screen and clicking the same controls a player would use. The action set is intentionally permission-gated rather than exposing unrestricted client internals.

## License

MCPs is released under the [MIT License](LICENSE).

Minecraft is a trademark of Mojang Studios. MCPs is not affiliated with or approved by Mojang Studios.