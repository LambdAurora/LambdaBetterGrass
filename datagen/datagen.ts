const better_snow = await Deno.readTextFile("datagen/better_snow.json")
	.then(text => JSON.parse(text));

class Identifier {
	constructor(public readonly namespace: string, public readonly path: string) {
	}

	to_string() {
		return `${this.namespace}:${this.path}`;
	}
}

function parse_id(raw_id: string): Identifier {
	if (!raw_id.includes(":"))
		return new Identifier("minecraft", raw_id);
	else {
		let id = raw_id.split(":");
		return new Identifier(id[0], id[1]);
	}
}

async function write_to_file(path: string, data: any): Promise<void> {
	await Deno.writeTextFile(path, JSON.stringify(data, null, "\t") + "\n");
}

function get_state_path(id: Identifier): string {
	return `src/main/resources/assets/${id.namespace}/bettergrass/states/${id.path}.json`;
}

interface StateJson {
	type: "layer";
	data?: string;
	variants?: Record<string, { data?: string }>;
}

interface BlockData {
	readonly id: string;
	readonly data: unknown;
	readonly custom_data_id?: string;
	readonly waterloggable?: boolean;
}
type Block = string | BlockData;

function make_state_json(block: Block, data_provider: (id: Identifier) => string) {
	let id;
	let data = undefined;
	let custom_data_id = undefined;
	let waterloggable = false;

	if (typeof block === "string") {
		id = parse_id(block);
	} else {
		id = parse_id(block.id);
		data = block.data;
		custom_data_id = block.custom_data_id;
		waterloggable = block.waterloggable !== undefined ? block.waterloggable : false;
	}

	let state_json: StateJson = {
		type: "layer",
		data: data_provider(custom_data_id !== undefined ? parse_id(custom_data_id) : id),
	};

	if (waterloggable) {
		state_json = {
			type: "layer",
			variants: {
				"waterlogged=false": {
					data: data_provider(id),
				},
			},
		};
	}

	return {id: id, path: get_state_path(id), json: state_json, data: data};
}

function get_data_path(id: Identifier): string {
	return `src/main/resources/assets/${id.namespace}/bettergrass/data/${id.path}.json`;
}

function make_data_json(options?: any): any {
	return Object.assign({}, options);
}

type GroupData = Block[] | {entries: Block[], data: object};

function get_group_data(raw: GroupData) {
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
		for (const block of (group_raw_data as Block[])) {
			const state_data = make_state_json(block, id => `${id.namespace}:bettergrass/data/${id.path}`);
			const data_path = get_data_path(state_data.id);

			await Promise.all([write_to_file(state_data.path, state_data.json), write_to_file(data_path, make_data_json(state_data.data))]);

			console.log(`Wrote better snow data for ${state_data.id.to_string()}.`);
		}
	} else {
		const group_data = get_group_data(group_raw_data as GroupData);
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
