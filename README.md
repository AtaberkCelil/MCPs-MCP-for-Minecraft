# MCPs - MCP for Minecraft

MCPs is a Fabric client mod that exposes a local Model Context Protocol endpoint so an MCP-capable AI client can inspect and control a Minecraft session.

## Status

The project targets Minecraft `26.2` and Fabric Loader `0.19.5`. The initial scaffold exposes the protocol endpoint at `http://127.0.0.1:25575/mcp`.

## Build

Install Java 25 and Gradle 9.5 or newer, then run:

```powershell
.\gradlew.bat build
```

The generated mod jar will be in `build/libs`.

## Connecting an MCP client

Add the endpoint as a Streamable HTTP MCP server using:

```text
http://127.0.0.1:25575/mcp
```

The endpoint binds to loopback only. Do not expose it directly to a network without adding authentication and an explicit permission model.

## Available tools

- `get_player_state`
- `get_nearby_blocks`
- `send_chat`
- `execute_command`

## License

MCPs is released under the [MIT License](LICENSE).

Minecraft is a trademark of Mojang Studios. MCPs is not affiliated with or approved by Mojang Studios.