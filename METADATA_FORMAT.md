# Metadata files

LambdaBetterGrass metadata files tells the mod how to handle a grass-like connection.

## Format

The root contains an array of JSON objects named `layers`.

Each layer can affect a `tint index`, but there is only one layer for each `tint index`.

### Layer object

The layer object contains the `textures` definition, the `masks` definition and the `color_index` property also known as `tint index`.

- `color_index` (optional)  
  The color index is an integer property with default value `-1`.
  
- `textures`
  - `top` - The top texture of the block.
  - `side` - The side texture of the block.
  - `overrides` (optional)  
    Enumerates custom texture which replace the corresponding generated textures.
    - `connect` - The texture applied to the side when the block is fully connected to an adjacent block.
    - `blend_up`
    - `blend_up_m` - Mirrored version of `blend_up`.
    - `arch`
    
- `masks` (optional)
  - `connect` - The mask texture applied to the side when the block is fully connected to an adjacent block.
  - `blend_up`
  - `arch`

## Examples

### Grass block

```json
{
  "layers": [
    {
      "textures": {
        "top": "lambdabettergrass:block/grass_block_side_shadow",
        "side": "minecraft:block/grass_block_side"
      },
      "masks": {
        "blend_up": "lambdabettergrass:bettergrass/mask/grass_block_side_underlay_blend_up",
        "arch": "lambdabettergrass:bettergrass/mask/grass_block_side_underlay_arch_blend"
      }
    },
    {
      "color_index": 0,
      "textures": {
        "top": "minecraft:block/grass_block_top",
        "side": "minecraft:block/grass_block_side_overlay"
      }
    }
  ]
}
```

### Warped Nylium

```json
{
  "layers": [
    {
      "textures": {
        "top": "minecraft:block/warped_nylium",
        "side": "minecraft:block/warped_nylium_side"
      }
    }
  ]
}
```
