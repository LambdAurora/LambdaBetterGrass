# Metadata States files

LambdaBetterGrass metadata states files tells how the mod handles the block.

## Type

There's multiple type of metadata states:

 - `grass`
 - `layer`
 
The `type` field specifies the type used.

## `grass` format

- `data` - The identifier of the metadata file, will apply this metadata file independently of the model variants.
 
OR

- `variants` - An object filed with fields corresponding to each of the variants.
  - `<variant_string>` - variant_string is a field with a name corresponding to the block model variant, for ex. grass blocks can be `snowy=false` or `snowy=true`, etc.
    - `data` - The identifier of the metadata file, will apply this metadata file independently of the model variants.
    
Not every variants need to have a metadata assigned.

## `layer` format

The layer format only requires:

```json
{
  "type": "layer"
}
```

To learn more about how to use the `layer` method, please go [here](https://github.com/LambdAurora/LambdaBetterGrass/blob/1.16/documentation/LAYER_METHOD.md).

## Examples

### Allium

```json
{
  "type": "layer"
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
