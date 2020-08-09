#!/usr/bin/env bash

name=$1

if [ "$2" != "" ]; then
  namespace=$2
else
  namespace="minecraft"
fi

mkdir -p src/main/resources/assets/${namespace}/bettergrass/states/ash
mkdir -p src/main/resources/assets/${namespace}/bettergrass/states/snow

state_path="src/main/resources/assets/${namespace}/bettergrass/states/${name}.json"
echo "Writing ${state_path}"
if [ -f "$state_path" ]; then
  rm ${state_path}
fi
cat >${state_path} <<EOL
{
  "type": "layer"
}
EOL

function write_metadata_file() {
    metadata_path="src/main/resources/assets/${namespace}/bettergrass/states/$1/${name}.json"
    echo "Writing ${metadata_path}"
    if [ -f "$metadata_path" ]; then
      rm ${metadata_path}
    fi
    cat >${metadata_path} <<EOL
{
  "layer": true
}
EOL
}

write_metadata_file ash
write_metadata_file snow
