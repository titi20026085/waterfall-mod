# Waterfall Mod
## ⚠️This mod is an early prototype, I have not tested compatibility with other mods and it not replace existing waterfalls or lavafalls
A lightweight Minecraft mod that limits the height of natural vertical waterfalls and lavafalls with visual evaporation effects.

## Features

- **Waterfall Height Limiting**: Prevents vertical waterfalls from extending beyond a configurable maximum height
- **Smart Detection**: Only affects true waterfalls (water columns surrounded by air on all 4 horizontal sides)
- **Visual Effects**: Spawns vanilla particle effects when waterfalls reach their limit
- **Configurable**: Fully customizable through configuration file
- **Multiplayer Compatible**: Works on both single-player and multiplayer servers
- **Performance Optimized**: Lightweight implementation with minimal impact on game performance

## Installation

1. Download the mod JAR file from the releases
2. Place the JAR file in your `mods` folder
3. Launch Minecraft with NeoForge installed
4. The mod will automatically generate a configuration file on first run

## Configuration

The mod configuration is located at `config/waterfall-common.toml` and includes:

```toml
#Enable or disable the mod entirely
enabled = true

[waterfall]
	#Maximum height (in blocks) of a vertical waterfall before it evaporates
	# Default: 12
	# Range: 1 ~ 100
	maxHeight = 12
	#Show cloud/splash particle effects when a waterfall evaporates
	enableEvaporationEffect = true
	#Density of evaporation particles (0.0 = none, 1.0 = maximum)
	# Default: 0.5
	# Range: 0.0 ~ 1.0
	particleDensity = 0.5

[lavafall]
	#Maximum height (in blocks) of a vertical lavafall before it evaporates
	# Default: 12
	# Range: 1 ~ 100
	maxHeight = 12
	#Show smoke/flame particle effects when a lavafall evaporates
	enableEvaporationEffect = true
```

## Technical Overview

### Waterfall Detection
A water column is considered a waterfall if:
- The flow is vertical and downward
- All 4 horizontal neighboring blocks (north, south, east, west) are air
- Contains flowing water or water source blocks

### What is NOT affected
- Oceans and rivers
- Water channels surrounded by blocks
- Aqueduct-style builds
- Any water touching solid blocks horizontally

### Evaporation Effects
When a waterfall reaches its height limit, the mod generates:
- **Cloud particles**: Representing water vapor
- **Falling Water particles**: Simulating water droplets

## Requirements

- **Minecraft**: 1.21.1
- **NeoForge**: 21.1.228 or higher
- **Java**: 21

## License

This mod is licensed under the MIT License.
