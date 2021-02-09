import * as fs from 'fs';

let better_snow = JSON.parse(fs.readFileSync('datagen/better_snow.json', 'utf-8'));

function parse_id(raw_id) {
    let result;
    if (!raw_id.includes(':'))
        result = {namespace: 'minecraft', path: raw_id};
    else {
        let id = raw_id.split(':');
        result = {namespace: id[0], path: id[1]};
    }
    result.to_string = function () {
        return `${this.namespace}:${this.path}`;
    }
    return result;
}

for (let block of better_snow) {
    let id;
    let waterloggable = false;

    if (typeof block === 'string') {
        id = parse_id(block);
    } else {
        id = parse_id(block.id);
        waterloggable = block.waterloggable;
    }

    let state_path = `src/main/resources/assets/${id.namespace}/bettergrass/states/${id.path}.json`;
    let data_path = `src/main/resources/assets/${id.namespace}/bettergrass/data/${id.path}.json`;

    let state_json = {
        type: 'layer',
        data: `${id.namespace}:bettergrass/data/${id.path}`
    };

    if (waterloggable) {
        state_json = {
            type: 'layer',
            variants: {
                'waterlogged=false': {
                    data: `${id.namespace}:bettergrass/data/${id.path}`
                }
            }
        }
    }

    fs.writeFileSync(state_path, JSON.stringify(state_json, null, 2), {encoding: 'utf-8'});

    fs.writeFileSync(data_path, JSON.stringify({
        snow: {
            layer: true
        },
        moss: {
            layer: true
        },
        ash: {
            layer: true
        }
    }, null, 2))

    console.log(`Wrote better snow data for ${id.to_string()}.`);
}