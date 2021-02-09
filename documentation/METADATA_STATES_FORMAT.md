# Metadata States files

LambdaBetterGrass metadata states files tells how the mod handles the block.

## Types

There's multiple type of metadata states:

 - [`grass`][metadata_grass]
 - [`layer`][metadata_layer]
 
The `type` field specifies the type used.

## Metadata definition

- `data` - The identifier of the metadata file, will apply this metadata file independently of the model variants.
 
OR

- `variants` - An object filed with fields corresponding to each of the variants.
  - `<variant_string>` - variant_string is a field with a name corresponding to the block model variant, for ex. grass blocks can be `snowy=false` or `snowy=true`, etc.
    - `data` - The identifier of the metadata file, will apply this metadata file depending on the model variants.
    
Not every variants need to have a metadata assigned.

## Examples

### Allium

```json
{
  "type": "layer",
  "data": "minecraft:bettergrass/data/allium"
}
```

### Grass block

```json
{
  "type": "grass",
  "variants": {
    "snowy=false": {
      "data": "minecraft:bettergrass/data/grass_block"
    },
    "snowy=true": {
      "data": "minecraft:bettergrass/data/snowy_grass_block"
    }
  }
}
```

### Warped Nylium

```json
{
  "type": "grass",
  "data": "minecraft:bettergrass/data/warped_nylium"
}
```

[metadata_grass]: https://github.com/LambdAurora/LambdaBetterGrass/blob/1.17/documentation/METADATA_GRASS_FORMAT.md "Grass Metadata Documentation"
[metadata_layer]: https://github.com/LambdAurora/LambdaBetterGrass/blob/1.17/documentation/LAYER_METHOD.md "Layer Metadata Documentation"
