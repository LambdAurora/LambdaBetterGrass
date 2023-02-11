# LambdaBetterGrass API

## Introduction and definitions

### Mask texture

A mask texture is used to generate the final texture.

The mask determine where the top texture should be applied on the side texture.

### Override texture

Override textures are textures which can entirely replace the generated texture which allow for better precision in texture edition.

### Metadata files

A metadata file defines rules and data depending on the [type][different types].

### Metadata states

[A metadata states][metadata_state] file defines how is a block handled in LambdaBetterGrass.

## Resource pack folders

### `assets/<namespace>/bettergrass/data` folder

This folder is where the metadata files for [different types] are stored.

### `assets/<namespace>/bettergrass/states` folder

This folder is where the [metadata states files][metadata_state] are stored.

They must have the same name as the model which it affects and be in the same namespace.

### `assets/lambdabettergrass/bettergrass/mask` folder

This folder stores the default mask texture.

A mask texture tells which texture should be copied using its alpha channel.

If at the specified pixel the alpha channel is equal to 255, then it uses the top texture, else it uses the side texture.

[metadata_state]: ./METADATA_STATES_FORMAT.md "Metadata State Documentation"
[different types]: ./METADATA_STATES_FORMAT.md#types "Metadata Types Documentation"
