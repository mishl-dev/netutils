# NetUtils

> **This tool is for educational and debugging purposes only.** Using it to gain an unfair advantage or perform exploits (such as item duplication) on multiplayer servers is a violation of most server rules and platform terms of service. The developers do not condone cheating/duping. Use this mod at your own risk and only where you have explicit permission.

A networking utility for protocol debugging and state synchronization testing.

<img width="1365" height="697" alt="image" src="https://github.com/user-attachments/assets/b84ae327-eb51-4112-ab92-cca66619e079" />



## Features
- **Packet Control**: Delay or toggle outgoing packets.
- **Synchronization**: Simulate client-server desynchronization for state handling tests.
- **Inventory Control**: Close inventory screens without sending packets to the server.
- **Resource Pack Control**: Bypass or force-deny server resource packs.

## Usage
Press **V** to restore the last closed screen.

## Client Commands
All commands use the `^` prefix and can be typed in regular chat or the custom chat input field.
- `^help` - Show all commands
- `^toggle` - Enable/disable mod features
- `^menuinfo` - Display GUI info (class, slots, sync ID)
- `^trades` - List villager trades with IDs
- `^trade <id>` - Automatically select and execute a trade
- `^click <slot> [button] [type]` - Click a specific inventory slot
- `^loop <n> <command>` - Repeat a command `n` times
- `^desync` - Synchronize/Desynchronize inventory state
- `^clear` - Clear the chat log
- `^packets` - List available packet types for `^rawsend`
- `^rawsend <times> <packet> [args]` - Force send raw packets to the server
- `^save`/`^load` - Save or restore the current screen state

## Building
Run the following command to build for both Fabric and NeoForge:
```bash
./gradlew collectJars
```
The output jars will be in `build/libs/`.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
