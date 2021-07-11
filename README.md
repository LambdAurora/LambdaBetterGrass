# LambdaBetterGrass

<!-- modrinth_exclude.start -->
![Java 16](https://img.shields.io/badge/language-Java%2016-9B599A.svg?style=flat-square) <!-- modrinth_exclude.end -->
[![GitHub license](https://img.shields.io/github/license/LambdAurora/LambdaBetterGrass?style=flat-square)](https://raw.githubusercontent.com/LambdAurora/LambdaBetterGrass/master/LICENSE)
![Environment: Client](https://img.shields.io/badge/environment-client-1976d2?style=flat-square)
[![Mod loader: Fabric]][fabric] <!-- modrinth_exclude.start -->
![Version](https://img.shields.io/github/v/tag/LambdAurora/LambdaBetterGrass?label=version&style=flat-square)
[![CurseForge](http://cf.way2muchnoise.eu/title/400322.svg)](https://www.curseforge.com/minecraft/mc-mods/lambdabettergrass)
<!-- modrinth_exclude.end -->

An actual better grass and snow mod for Fabric.

## 📖 What's this mod?

Have you ever used the MCPatcher/OptiFine better grass feature?
I did, and I didn't like it at all.
The main issue is that it's too rough even at Fancy settings.
That's why I made my own mod to fix this issue and provide an alternative to one of the OptiFine features to the Fabric ecosystem!
Grass blocks will now connect, and the nearby grass blocks will be smoothed.

![pack.png with better grass](images/pack.png)

![mountain with better grass on grass and snowy grass](images/better_grass.png)

![Warped forest with better grass](images/better_grass_warped_forest.png)

![Better snow](images/better_snow.png)

Better snow with the optional included resource pack:
![Optional better snow](images/better_snow_resource_pack.png)

Better "snow" with the optional included resource pack in lush caves (credits to cavebiomes by SuperCoder79):

![Optional better moss](images/better_moss.png)

Configuration screen:
![config screen](images/config_screen.png)

Searching other mods to replace OptiFine? [Check out this list!](https://gist.github.com/LambdAurora/1f6a4a99af374ce500f250c6b42e8754)

## ✅ Features:

- Connect grass blocks
- Connect mycelium blocks
- Connect podzol blocks
- Connect grass path blocks
- Connect crimson nylium blocks
- Connect warped nylium blocks
- Add snow layer to many non-full blocks when Better Snow is enabled
- Add moss layer to many non-full blocks when Better Snow is enabled
- Add an optional built-in resource pack to have snowy fences.
- API for resource pack creators and modders to add new block connection following the better grass rule or new alternate models for blocks following the better snow rule.
- And more!

## 📖 Usage

Using this mod is very simple!

Install it in your mods folder along with [Fabric API] and [ModMenu].

You will notice that some blocks like grass blocks will connect together, if you want to try other options or want to have the same look as OptiFine you can look into the settings screen of the mod via [ModMenu].

<!-- modrinth_exclude.start -->
### Build

Just do `./gradlew shadowRemapJar` and everything should build just fine!
<!-- modrinth_exclude.end -->

### For resource packs creators and developers

Please check out [this documentation](https://github.com/LambdAurora/LambdaBetterGrass/blob/1.17/documentation/API.md).

## 📖 Compatibility

- [Sodium] is currently incompatible as it is missing the required Fabric Rendering API implementation which will be added in the future.
- Canvas is compatible.
- ConnectedBlockTextures is compatible.
- OptiFabric is obviously incompatible.

[fabric]: https://fabricmc.net
[Mod loader: Fabric]: https://img.shields.io/badge/modloader-Fabric-1976d2?style=flat-square&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAAsTAAALEwEAmpwYAAAFHGlUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4gPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iQWRvYmUgWE1QIENvcmUgNS42LWMxNDIgNzkuMTYwOTI0LCAyMDE3LzA3LzEzLTAxOjA2OjM5ICAgICAgICAiPiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPiA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyIgeG1sbnM6cGhvdG9zaG9wPSJodHRwOi8vbnMuYWRvYmUuY29tL3Bob3Rvc2hvcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RFdnQ9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZUV2ZW50IyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgQ0MgMjAxOCAoV2luZG93cykiIHhtcDpDcmVhdGVEYXRlPSIyMDE4LTEyLTE2VDE2OjU0OjE3LTA4OjAwIiB4bXA6TW9kaWZ5RGF0ZT0iMjAxOS0wNy0yOFQyMToxNzo0OC0wNzowMCIgeG1wOk1ldGFkYXRhRGF0ZT0iMjAxOS0wNy0yOFQyMToxNzo0OC0wNzowMCIgZGM6Zm9ybWF0PSJpbWFnZS9wbmciIHBob3Rvc2hvcDpDb2xvck1vZGU9IjMiIHBob3Rvc2hvcDpJQ0NQcm9maWxlPSJzUkdCIElFQzYxOTY2LTIuMSIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDowZWRiMWMyYy1mZjhjLWU0NDEtOTMxZi00OTVkNGYxNGM3NjAiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6MGVkYjFjMmMtZmY4Yy1lNDQxLTkzMWYtNDk1ZDRmMTRjNzYwIiB4bXBNTTpPcmlnaW5hbERvY3VtZW50SUQ9InhtcC5kaWQ6MGVkYjFjMmMtZmY4Yy1lNDQxLTkzMWYtNDk1ZDRmMTRjNzYwIj4gPHhtcE1NOkhpc3Rvcnk+IDxyZGY6U2VxPiA8cmRmOmxpIHN0RXZ0OmFjdGlvbj0iY3JlYXRlZCIgc3RFdnQ6aW5zdGFuY2VJRD0ieG1wLmlpZDowZWRiMWMyYy1mZjhjLWU0NDEtOTMxZi00OTVkNGYxNGM3NjAiIHN0RXZ0OndoZW49IjIwMTgtMTItMTZUMTY6NTQ6MTctMDg6MDAiIHN0RXZ0OnNvZnR3YXJlQWdlbnQ9IkFkb2JlIFBob3Rvc2hvcCBDQyAyMDE4IChXaW5kb3dzKSIvPiA8L3JkZjpTZXE+IDwveG1wTU06SGlzdG9yeT4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz4/HiGMAAAAtUlEQVRYw+XXrQqAMBQF4D2P2eBL+QIG8RnEJFaNBjEum+0+zMQLtwwv+wV3ZzhhMDgfJ0wUSinxZUQWgKos1JP/AbD4OneIDyQPwCFniA+EJ4CaXm4TxAXCC0BNHgLhAdAnx9hC8PwGSRtAFVMQjF7cNTWED8B1cgwW20yfJgAvrssAsZ1cB3g/xckAxr6FmCDU5N6f488BrpCQ4rQBJkiMYh4ACmLzwOQF0CExinkCsvw7vgGikl+OotaKRwAAAABJRU5ErkJggg==
[Fabric API]: https://modrinth.com/mod/fabric-api "Fabric API Modrinth page"
[ModMenu]: https://modrinth.com/mod/modmenu "ModMenu Modrinth page"
[Sodium]: https://modrinth.com/mod/sodium "Sodium Modrinth page"
