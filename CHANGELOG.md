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

### 1.1.0

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

### 1.2.0

 - Added a way to disable temporarily the better layer feature. Thread-bound.
 - Fixed an issue in layer data loading, it wasn't respecting metadata namespace.

[SpruceUI]: https://github.com/LambdAurora/SpruceUI
[Connected Block Textures]: https://www.curseforge.com/minecraft/mc-mods/connected-block-textures "Connected Block Textures CurseForge page"
