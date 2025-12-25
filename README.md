# NetUtils

A Network utility mod for dupe hunting and packet manipulation.

## Features
- **Packet Control**: Delay or toggle outgoing packets.
- **De-sync**: Intentionally desynchronize your client from the server (Ghost Mode).
- **Inventory Control**: Close inventory screens without sending packets to the server.
- **Resource Pack Control**: Bypass or force-deny server resource packs.

## Usage
Press **V** to restore the last closed screen.

## Building
Run the following command to build for both Fabric and NeoForge:
```bash
./gradlew collectJars
```
The output jars will be in `build/libs/`.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
