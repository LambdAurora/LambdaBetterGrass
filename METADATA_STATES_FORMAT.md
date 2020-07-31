# Metadata States files

LambdaBetterGrass metadata states files tells the mod which metadata file to use with which block model variant.

## Format

- `data` - The identifier of the metadata file, will apply this metadata file independently of the model variants.
 
OR

- `variants` - An object filed with fields corresponding to each variants.
  - `<variant_string>` - variant_string is a field with a name corresponding to the block model variant, like for grass blocks there is `snowy=false` or `snowy=true`, etc.
    - `data` - The identifier of the metadata file, will apply this metadata file independently of the model variants.
    
Not every variants need to have a metadata assigned.

## Examples

### Grass block

```json
{
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
  "data": "minecraft:bettergrass/data/warped_nylium"
}
```
