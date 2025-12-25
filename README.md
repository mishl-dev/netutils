# NetUtils

A networking utility for protocol debugging and state synchronization testing.

## Features
- **Packet Control**: Delay or toggle outgoing packets.
- **Synchronization**: Simulate client-server desynchronization for state handling tests.
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
