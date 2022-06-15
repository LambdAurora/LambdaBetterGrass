export const DECODER = new TextDecoder("utf-8");
export const ENCODER = new TextEncoder();

const better_snow = await Deno.readFile("datagen/better_snow.json")
	.then(source => DECODER.decode(source))
	.then(text => JSON.parse(text));

class Identifier {
	constructor(namespace, path) {
		this.namespace = namespace;
		this.path = path;
	}

	to_string() {
		return `${this.namespace}:${this.path}`;
	}
}

function parse_id(raw_id) {
	if (!raw_id.includes(':'))
		return new Identifier('minecraft', raw_id);
	else {
		let id = raw_id.split(':');
		return new Identifier(id[0], id[1]);
	}
}

async function write_to_file(path, data) {
	await Deno.writeFile(path, ENCODER.encode(JSON.stringify(data, null, 2)));
}

function get_state_path(id) {
	return `src/main/resources/assets/${id.namespace}/bettergrass/states/${id.path}.json`;
}

function make_state_json(block, data_provider) {
	let id;
	let data = undefined;
	let custom_data_id = undefined;
	let waterloggable = false;

	if (typeof block === 'string') {
		id = parse_id(block);
	} else {
		id = parse_id(block.id);
		data = block.data;
		custom_data_id = block.custom_data_id;
		waterloggable = block.waterloggable !== undefined ? block.waterloggable : false;
	}

	let state_json = {
		type: 'layer',
		data: data_provider(custom_data_id !== undefined ? parse_id(custom_data_id) : id)
	};

	if (waterloggable) {
		state_json = {
			type: 'layer',
			variants: {
				'waterlogged=false': {
					data: data_provider(id)
				}
			}
		}
	}

	return {id: id, path: get_state_path(id), json: state_json, data: data};
}

function get_data_path(id) {
	return `src/main/resources/assets/${id.namespace}/bettergrass/data/${id.path}.json`;
}

function make_data_json(options) {
	return Object.assign({}, options);
}

function get_group_data(raw) {
	let entries;
	let data = make_data_json();
	if (raw instanceof Array) {
		entries = raw;
	} else {
		entries = raw.entries;
		if (raw.data !== undefined) {
			data = make_data_json(raw.data);
		}
	}

	return {entries: entries, data: data};
}

for (const [group, group_raw_data] of Object.entries(better_snow)) {
	if (group === "global") {
		for (let block of group_raw_data) {
			let state_data = make_state_json(block, id => `${id.namespace}:bettergrass/data/${id.path}`);
			let data_path = get_data_path(state_data.id);

			await Promise.all([write_to_file(state_data.path, state_data.json), write_to_file(data_path, make_data_json(state_data.data))]);

			console.log(`Wrote better snow data for ${state_data.id.to_string()}.`);
		}
	} else {
		const group_data = get_group_data(group_raw_data);
		const data_id = `minecraft:bettergrass/data/${group}`;

		console.log(`Writing better snow data for group ${group} (${group_data.entries.length} entries)...`);

		await write_to_file(get_data_path(parse_id(group)), group_data.data);

		let i = 0;
		for (let block of group_data.entries) {
			i++;

			let state_data = make_state_json(block, _ => data_id);
			await write_to_file(state_data.path, state_data.json);

			console.log(`  => Wrote better snow data for ${state_data.id.to_string()} (${i}/${group_data.entries.length} entries).`);
		}
	}
}
