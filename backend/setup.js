import { mkdir, writeFile, rename } from 'node:fs/promises';
const source =
  process.env.CATALOG_SOURCE_URL ??
  'https://movie-api-delta-six.vercel.app/catalog';
const response = await fetch(source, { signal: AbortSignal.timeout(60_000) });
if (!response.ok)
  throw new Error(`Catalog download failed: HTTP ${response.status}`);
const body = await response.text();
const catalog = JSON.parse(body);
if (
  !Array.isArray(catalog.movies) ||
  !catalog.movies.length ||
  !Array.isArray(catalog.trivia)
) {
  throw new Error(
    'Expected a normalized catalog with movies and trivia arrays',
  );
}
if (
  new Set(catalog.movies.map((movie) => movie.id)).size !==
  catalog.movies.length
) {
  throw new Error('Duplicate movie IDs');
}
await mkdir(new URL('../data/', import.meta.url), { recursive: true });
const temporary = new URL('../data/movies.json.tmp', import.meta.url);
await writeFile(temporary, body);
await rename(temporary, new URL('../data/movies.json', import.meta.url));
console.log(
  `Downloaded ${catalog.movies.length} movies into ignored local data/movies.json`,
);
