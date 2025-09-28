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
- Added Traditional Chinese translations ([#79](https://github.com/LambdAurora/LambdaBetterGrass/pull/79)).
- Updated [SpruceUI].

### 1.5.1

- Added Brazilian Portuguese translations ([#85](https://github.com/LambdAurora/LambdaBetterGrass/pull/85)).
- Fixed several issues with custom resource packs with custom grass models ([#48](https://github.com/LambdAurora/LambdaBetterGrass/issues/48) and partially [#76](https://github.com/LambdAurora/LambdaBetterGrass/issues/76)).
- (1.20) Added better snow data to the new Minecraft blocks.

### 1.5.2

- Updated model injections to use Fabric API's new model loading API which should improve mod compatibility.
- Added Vietnamese translations ([#90](https://github.com/LambdAurora/LambdaBetterGrass/pull/90)).

## 2.0.0

- Switched back to Fabric.
- Refactored runtime texture generation injection to directly inject into the atlas instead of using a virtual resource pack.
- Refactored configuration loading and saving to be more reliable and avoid corruption.
- Reworked layer type format and rendering.
  - Layer types now specify a block state to display.
  - Custom models are not needed anymore, culling works as intended. This means layers will better adapt to resource packs. Fixes a part of [#76](https://github.com/LambdAurora/LambdaBetterGrass/issues/76).
  - Render types are not needed anymore either, the correct render type is applied on the quads who need them.
  - Layer types can now define which blocks it should match against.
  - Layers are still having issues with Iris' shaderpacks making them wavy on plants.
- Added chorus plants, and many more missing blocks to the better snow system.
- Added glow lichen as a layer type.
- Added a configuration tab in Sodium's video settings.
- Added Swedish translations ([#94](https://github.com/LambdAurora/LambdaBetterGrass/pull/94)).
- Added Catalan translations ([#104](https://github.com/LambdAurora/LambdaBetterGrass/pull/104)).
- Added Malay translations ([#110](https://github.com/LambdAurora/LambdaBetterGrass/pull/110)).
- Fixed snowy grass not always properly connecting when better snow is active.
- Updated [SpruceUI].

### 2.0.1

- Fixed bad cake layer data.

### 2.0.2

- Fixed some cases where the underneath grass block wasn't displayed as snowy when a snow layer was added on top of it using better snow.

### 2.0.3

- Fixed crash with Sodium while rendering some better grass blocks ([#114](https://github.com/LambdAurora/LambdaBetterGrass/issues/114)).
    - Note: this may not affect this version as this was discovered on 1.21.8, but the faulty code path is the same.

### 2.0.4

- Updated [SpruceUI].
- Updated [Yumi Minecraft Libraries: Foundation].
  - This may fix some synchronization issues on Fabric.

## 2.1.0

- Updated to Minecraft 1.21.4 (thanks to [#113](https://github.com/LambdAurora/LambdaBetterGrass/pull/113) for the help).
  - Added pale moss layer type.
  - Added new relevant blocks to the better snow system.
  - (Built-in Default addon) Added textures for the new fence and pale moss layer to fences.
- Updated [SpruceUI].

### 2.1.1

- Fixed crash with Sodium while rendering some better grass blocks ([#114](https://github.com/LambdAurora/LambdaBetterGrass/issues/114)).
    - Note: this may not affect this version as this was discovered on 1.21.8, but the faulty code path is the same.

### 2.1.2

- Updated [SpruceUI].
- Updated [Yumi Minecraft Libraries: Foundation].
  - This may fix some synchronization issues on Fabric.

## 2.2.0

- Updated to Minecraft 1.21.5.
  - Added leaf litter layer type.
  - Added new relevant blocks to the better snow system.
- Added missing potted plants to the better snow system.
- Updated [SpruceUI].

### 2.2.1

- Fixed crash with Sodium while rendering some better grass blocks ([#114](https://github.com/LambdAurora/LambdaBetterGrass/issues/114)).
    - Note: this may not affect this version as this was discovered on 1.21.8, but the faulty code path is the same.

### 2.2.2

- Updated [SpruceUI].
- Updated [Yumi Minecraft Libraries: Foundation].
  - This may fix some synchronization issues on Fabric.

## 2.3.0

- Updated to Minecraft 1.21.8.
  - Added Dried Ghast to the better snow system.
- Updated [SpruceUI].

### 2.3.1

- Fixed crash with Sodium while rendering some better grass blocks ([#114](https://github.com/LambdAurora/LambdaBetterGrass/issues/114)).

### 2.3.2

- Updated [SpruceUI].
- Updated [Yumi Minecraft Libraries: Foundation].
  - This may fix some synchronization issues on Fabric.

[SpruceUI]: https://github.com/LambdAurora/SpruceUI
[Yumi Minecraft Libraries: Foundation]: https://github.com/YumiProject/yumi-minecraft-foundation-library "Yumi Minecraft Foundation Library page"
[Connected Block Textures]: https://www.curseforge.com/minecraft/mc-mods/connected-block-textures "Connected Block Textures CurseForge page"
