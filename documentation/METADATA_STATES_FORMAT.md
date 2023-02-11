# Metadata States files

LambdaBetterGrass metadata states files tells how the mod handles the block.

Metadata state files are placed in the `assets/<namespace>/bettergrass/states/` directory,
with `<namespace>` the namespace used by the block to handle (if the block identifier is `minecraft:podzol` then `<namespace>` will be `minecraft`).  
Each state file is named after the block identifier and `.json` appended at the end,
like resource pack block state files.
For example, if the block identifier is `minecraft:podzol` then the file will be named `podzol.json`.

## Types

There's multiple type of metadata states:

 - [`grass`][metadata_grass] - for specifying how grass-like blocks connect to each other
 - [`layer`][metadata_layer] - for specifying how layer-like (like snow, moss carpets, etc.) blocks should be treated on the targeted block
 
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

[metadata_grass]: ./METADATA_GRASS_FORMAT.md "Grass Metadata Documentation"
[metadata_layer]: ./LAYER_METHOD.md "Layer Metadata Documentation"
