# MCPs development notes

This project is a Fabric `26.2` client mod with a local Model Context Protocol endpoint.

- MCP reference: https://modelcontextprotocol.io/
- Official TypeScript SDK reference: https://github.com/modelcontextprotocol/typescript-sdk
- Fabric development reference: https://docs.fabricmc.net/
- Keep network handlers off the Minecraft client thread; marshal game access through the client tick.
- Treat every mutating tool as privileged and keep the endpoint bound to `127.0.0.1`.