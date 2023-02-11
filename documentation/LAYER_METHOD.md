# Metadata Layer Method

The layer method is used for the better snow feature.

## Layer types

A layer references a block that is layered like the snow block, or the ash block from Cinderscapes.

A layer type is defined by a JSON file in `assets/<namespace>/bettergrass/layer_types/<name>.json`.

### Layer type format

The layer type format is very simple:

 - `block` - The identifier of the layered block (like `minecraft:snow`).
 - `model` - A model path to use as layer.
 
#### Example

From `assets/lambdabettergrass/bettergrass/layer_types/snow.json`
```json
{
  "block": "minecraft:snow",
  "model": "lambdabettergrass:block/snowy_layer"
}
```

## Metadata state

When a block uses the `layer` method, it uses the metadata file determine by the rules provided by the [metadata state file][metadata_state]

### Format

The root object will contain fields for each layer type to override.

Each layer type object contain:
 - `layer` - True if the mod should add the layer model.
 - `offset` (optional) - A 3-component array representing the XYZ offset of the original model. Doesn't apply if a custom model is provided.
 - `block_state` (optional) - A custom block state to provide a custom model of the block state, useful to provide snowy variations for example.

Layer metadata files from different resource packs are merged.

#### Resource Pack Merging Example

We have two resource packs, A and B, A as a higher priority over B.

If resource pack A defines the metadata file for "snow" and "ash", and resource pack B defines the metadata file for "snow" and "moss".

LambdaBetterGrass will use the rules of resource pack A for "snow" and "ash", and will use the rules of resource pack B for "moss".

### Examples

#### Lilac

In `assets/minecraft/bettergrass/states/lilac.json`:
```json
{
  "type": "layer",
  "data": "minecraft:bettergrass/data/lilac"
}
```

In `assets/minecraft/bettergrass/data/lilac.json`:
```json
{
  "snow": {
    "layer": true
  },
  "moss": {
    "layer": true
  },
  "ash": {
    "layer": true
  }
}
```

#### Brewing Stand

In `assets/minecraft/bettergrass/states/brewing_stand.json`:

```json
{
  "type": "layer",
  "data": "minecraft:bettergrass/data/brewing_stand"
}
```

In `assets/minecraft/bettergrass/data/brewing_stand.json`:

```json
{
  "snow": {
    "layer": true,
    "offset": [
      0,
      0.001,
      0
    ]
  },
  "moss": {
    "layer": true
  },
  "ash": {
    "layer": true
  }
}
```

#### Oak fence

In `assets/minecraft/bettergrass/states/oak_fence.json`:
```json
{
  "type": "layer",
  "variants": {
    "waterlogged=false": {
      "data": "minecraft:bettergrass/data/oak_fence"
    }
  }
}
```

In `assets/minecraft/bettergrass/data/oak_fence.json` from the default extension resource pack (only overrides snow and moss layers):
```json
{
  "snow": {
    "layer": true,
    "block_state": {
      "multipart": [
        {
          "apply": {
            "model": "lambdabettergrass:block/fence/snowy_oak_fence_post"
          }
        },
        {
          "when": {
            "north": "true"
          },
          "apply": {
            "model": "lambdabettergrass:block/fence/snowy_oak_fence_side"
          }
        },
        {
          "when": {
            "east": "true"
          },
          "apply": {
            "model": "lambdabettergrass:block/fence/snowy_oak_fence_side",
            "y": 90
          }
        },
        {
          "when": {
            "south": "true"
          },
          "apply": {
            "model": "lambdabettergrass:block/fence/snowy_oak_fence_side",
            "y": 180
          }
        },
        {
          "when": {
            "west": "true"
          },
          "apply": {
            "model": "lambdabettergrass:block/fence/snowy_oak_fence_side",
            "y": 270
          }
        }
      ]
    }
  },
  "moss": {
    "layer": true,
    "block_state": {
      "multipart": [
        {
          "apply": {
            "model": "lambdabettergrass:block/fence/mossy_oak_fence_post"
          }
        },
        {
          "when": {
            "north": "true"
          },
          "apply": {
            "model": "lambdabettergrass:block/fence/mossy_oak_fence_side"
          }
        },
        {
          "when": {
            "east": "true"
          },
          "apply": {
            "model": "lambdabettergrass:block/fence/mossy_oak_fence_side",
            "y": 90
          }
        },
        {
          "when": {
            "south": "true"
          },
          "apply": {
            "model": "lambdabettergrass:block/fence/mossy_oak_fence_side",
            "y": 180
          }
        },
        {
          "when": {
            "west": "true"
          },
          "apply": {
            "model": "lambdabettergrass:block/fence/mossy_oak_fence_side",
            "y": 270
          }
        }
      ]
    }
  }
}
```

[metadata_state]: ./METADATA_STATES_FORMAT.md "Metadata State Documentation"
