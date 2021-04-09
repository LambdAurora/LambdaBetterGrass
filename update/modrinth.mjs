import * as fs from 'fs';
import * as process from 'process';
import fetch from 'node-fetch';
import FormData from 'form-data';

const MODRINTH_API = 'https://api.modrinth.com/api/v1/'
const MODRINTH_TOKEN = process.env.MODRINTH_TOKEN;
const MOD_ID = '2Uev7LdA';

const CURRENT_TAG = process.env.GITHUB_REF;

const EXCLUDE_REGEX = /<!-- modrinth_exclude\.start -->(.|\n)*?<!-- modrinth_exclude\.end -->/gm;
const LINK_REGEX = /!\[([A-z_ ]+)]\((images\/[A-z./_]+)\)/g;
const MINECRAFT_VERSION_REGEX = /minecraft_version=(.+)/g;
const MOD_VERSION_REGEX = /mod_version=(.+)/g;
const ARCHIVES_BASE_NAME_REGEX = /archives_base_name=([a-z_-]+)/g;

function parse_readme() {
  let readme = fs.readFileSync('README.md', {encoding: 'utf8'});
  readme = readme.replace(EXCLUDE_REGEX, '');
  readme = readme.replace(LINK_REGEX, '![$1](https://raw.githubusercontent.com/Queerbric/Inspecio/1.17/$2)');
  return readme;
}

function modrinth_fetch(path, method, body) {
  let options = {
    method: method,
    headers: {'Authorization': MODRINTH_TOKEN}
  };
  if (body !== undefined && body !== null) {
    if (body instanceof FormData) {
      options.headers = {...options.headers, ...body.getHeaders()};
    } else
      options.headers['Content-Type'] = 'application/json';
    options.body = body;
  }
  return fetch(MODRINTH_API + path, options);
}

modrinth_fetch('mod/' + MOD_ID, 'PATCH', JSON.stringify({
  body: parse_readme()
})).then(response => {
  if (response.status === 200) console.log('Successfully updated modrinth mod body.');
  else console.log('An error happened while updating modrinth mod body.', response);
});

// Parse gradle.properties for version yay
let properties = fs.readFileSync('gradle.properties', {encoding: 'utf8'});
let mc_version = MINECRAFT_VERSION_REGEX.exec(properties)[1];
let mod_version = MOD_VERSION_REGEX.exec(properties)[1];
let archives_base_name = ARCHIVES_BASE_NAME_REGEX.exec(properties)[1];

function get_mc_version_string() {
  if (mc_version.match(/^\d\dw\d\d[a-z]$/)) {
    return mc_version;
  }
  let last_dot = mc_version.lastIndexOf('.');
  return mc_version.substring(0, last_dot);
}

let file_mc_version = get_mc_version_string();
let full_mod_version = `${mod_version}+${file_mc_version}`;

let file = `${archives_base_name}-${full_mod_version}.jar`;

function try_publish() {
  fetch('https://api.github.com/repos/Queerbric/Inspecio/releases/tags/' + CURRENT_TAG, {
    method: 'GET',
    headers: {
      Accept: 'application/vnd.github.v3+json'
    }
  }).then(response => {
    if (response.status === 202) {
      setTimeout(try_publish, 2000);
    } else {
      return response.json();
    }
  }).then(json => {
    let form = new FormData();
    form.append('data', JSON.stringify({
      mod_id: MOD_ID,
      file_parts: [file],
      version_number: full_mod_version,
      version_title: `LambdaBetterGrass v${mod_version} (${mc_version})`,
      version_body: json.body,
      dependencies: [],
      game_versions: [mc_version],
      release_channel: 'release',
      loaders: ['fabric'],
      featured: !mc_version.match(/^\d\dw\d\d[a-z]$/)
    }));
    form.append(file, fs.createReadStream('build/libs/' + file), file);

    modrinth_fetch('version', 'POST', form).then(response => response.json()).then(response => console.log(response));
  });
}

try_publish();
