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

When a block uses the `layer` method, for each layer type there is an optional metadata state file.

The metadata state files are located in `assets/<namespace>/bettergrass/states/<layer_type>/<name>.json`

The name part must be the same as the name of the metadata state file that specify that a block uses the layer method.

### Format

 - `layer` - True if the mod should add the layer model.
 - `block_state` - A custom block state to provide a custom model of the block state, useful to provide snowy variations for example.

### Examples

#### Lilac

In `assets/minecraft/bettergrass/states/lilac.json`:
```json
{
  "type": "layer"
}
```

In `assets/minecraft/bettergrass/states/snow/lilac.json`:
```json
{
  "layer": true
}
```

#### Oak fence

In `assets/minecraft/bettergrass/states/oak_fence.json`:
```json
{
  "type": "layer"
}
```

In `assets/minecraft/bettergrass/states/snow/oak_fence.json` from the default extension resource pack:
```json
{
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
}
```
