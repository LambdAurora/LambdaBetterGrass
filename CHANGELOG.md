# LambdaBetterGrass Changelog

## 1.0.0 - :tada: Initial release! :tada:

The first release of LambdaBetterGrass!

 - Adds better grass to the game.
   - Some grass-like blocks will connect to each other.
 - Adds better snow to the game.
   - Some non full-blocks when surrounded by snow will get a snowy variation or just the snow layer.
 - Adds an optional built-in resource pack as an extension for better snow to get snowy fences.
 - API for resource packs and mods through JSON files
   - Allows adding better grass to modded blocks.
   - Allows adding new model variation for better snow.
   - Allows other snow-like blocks like ash from Cinderscapes to have better snow features.
 - Translated in:
   - English
   - French
   - Simplified Chinese ([#3](https://github.com/LambdAurora/LambdaBetterGrass/pull/3))
   - Polish ([#7](https://github.com/LambdAurora/LambdaBetterGrass/pull/7))
   - Mexican Spanish ([#5](https://github.com/LambdAurora/LambdaBetterGrass/pull/5))
 - And more!
 
### 1.0.1

 - Fixed incompatibility with [Connected Block Textures].

### 1.0.2

 - Tweaked better grass connection of snowy blocks with better snow.
 - Fixed some lighting issues with better grass. 
 - Updated [SpruceUI].

### 1.0.3

 - Tweaked better grass logic to be more thread-safe.

## 1.1.0

 - Added "moss" layer for 1.17.
   - (Resource Pack Extension) Added mossy fences.
 - Added more layer definitions for non-full block.
 - Changed "layer" format to reduce file count.
   - [Updated documentation](https://github.com/LambdAurora/LambdaBetterGrass/blob/1.17/documentation/LAYER_METHOD.md).
 - Updated [SpruceUI].
 - \[Internal] Changed package name.
 - \[Internal] Rewrote data-gen (changed from shell script to JS script).

### 1.1.1

 - Fix podzol better grass.
 - Updated [SpruceUI].

### 1.1.2

 - Added settings button to video settings.
 - Minor optimization.
 - Updated to Java 16.
 - Updated [SpruceUI].

## 1.2.0

 - Added a way to disable temporarily the better layer feature. Thread-bound.
 - Fixed an issue in layer data loading, it wasn't respecting metadata namespace.

### 1.2.1

 - Added a lot of missing better snow/moss data for blocks like walls, [lightning_rod](https://github.com/LambdAurora/LambdaBetterGrass/issues/17), etc.
 - Added an `offset` field to the `better_layer` data structure.
 - Added Estonian translations ([#23](https://github.com/LambdAurora/LambdaBetterGrass/pull/23)).
 - Improved chunk rebuild performance by caching hasBetterLayer ([#21](https://github.com/LambdAurora/LambdaBetterGrass/pull/21)).
 - Fixed better grass not connecting properly with better snow ([#25](https://github.com/LambdAurora/LambdaBetterGrass/issues/25)).
 - Updated [SpruceUI].

### 1.2.2

- Added Russian translations ([#26](https://github.com/LambdAurora/LambdaBetterGrass/pull/26)).
- Fixed layer block detection in better snow.
- Updated [SpruceUI].

### 1.2.3

- Added Turkish translations ([#35](https://github.com/LambdAurora/LambdaBetterGrass/pull/35)).
- Updated to Minecraft 1.18.2.
- Updated [SpruceUI].

### 1.2.4

- Fixed grass connecting to obstructed grass ([#29](https://github.com/LambdAurora/LambdaBetterGrass/pull/29)).
- Fixed random startup crashes ([#37](https://github.com/LambdAurora/LambdaBetterGrass/pull/37)).
- Made data files default every non-explicit layers to `{ layer: true }`.
- \[meta] Fixed compatibility changes being outdated on mod pages.

## 1.3.0

- Added a way for layer types to request a render layer for blocks.
- Updated to Minecraft 1.19.
  - Added sculk vein layer type.
  - Added bunch of blocks to the better snow system.
- Updated [SpruceUI].

## 1.4.0

- Switched to [Quilt](https://quiltmc.org) to allow easier maintenance in the future due to the APIs.
  Fabric mods work with Quilt.
- Updated to Minecraft 1.19.3
  - Added bunch of blocks to the better snow system.
- Moved the default mycelium connection textures to the default built-in resource pack.
- Updated [SpruceUI].

## 1.5.0

- Updated to Minecraft 1.19.4
  - Added bunch of blocks to the better snow system.
- Updated [SpruceUI].

[SpruceUI]: https://github.com/LambdAurora/SpruceUI
[Connected Block Textures]: https://www.curseforge.com/minecraft/mc-mods/connected-block-textures "Connected Block Textures CurseForge page"
