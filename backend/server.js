import { readFile } from 'node:fs/promises';
import { createServer } from 'node:http';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const catalogPath = new URL('../data/movies.json', import.meta.url);
const fold = (value) => value.toLocaleLowerCase('en');

export function createHandler(catalog) {
  const movies = catalog.movies;
  const byId = new Map(movies.map((movie) => [movie.id, movie]));
  const genres = [...new Set(movies.flatMap((movie) => movie.genres))].sort();
  const languages = [
    ...new Set(movies.map((movie) => movie.originalLanguage).filter(Boolean)),
  ].sort();
  return (req, res) => {
    const send = (status, body) => {
      res.writeHead(status, {
        'Content-Type': 'application/json; charset=utf-8',
        'X-Content-Type-Options': 'nosniff',
      });
      res.end(req.method === 'HEAD' ? undefined : JSON.stringify(body));
    };
    if (req.method !== 'GET' && req.method !== 'HEAD') {
      res.setHeader('Allow', 'GET, HEAD');
      return send(405, { error: 'Method not allowed' });
    }
    try {
      const url = new URL(req.url, 'http://localhost');
      const params = url.searchParams;
      if (url.pathname === '/health')
        return send(200, { status: 'ok', movies: movies.length });
      if (url.pathname === '/catalog') return send(200, catalog);
      if (url.pathname === '/genres') return send(200, { genres, languages });
      if (url.pathname.startsWith('/movies/')) {
        const movie = byId.get(
          decodeURIComponent(url.pathname.slice('/movies/'.length)),
        );
        return movie
          ? send(200, movie)
          : send(404, { error: 'Movie not found' });
      }
      if (url.pathname !== '/movies')
        return send(404, { error: 'Route not found' });
      const allowed = new Set([
        'q',
        'genre',
        'language',
        'year',
        'minRating',
        'sort',
        'order',
        'limit',
        'offset',
      ]);
      for (const key of params.keys()) {
        if (!allowed.has(key) || params.getAll(key).length !== 1) {
          return send(400, { error: `Unknown or repeated parameter: ${key}` });
        }
      }
      function number(name, fallback, min, max, integer = true) {
        if (!params.has(name)) return fallback;
        const raw = params.get(name);
        const value = Number(raw);
        if (
          !raw.trim() ||
          !Number.isFinite(value) ||
          (integer && !Number.isInteger(value)) ||
          value < min ||
          value > max
        ) {
          throw new RangeError(
            `Invalid ${name}: expected ${integer ? 'integer' : 'number'} between ${min} and ${max}`,
          );
        }
        return value;
      }
      const limit = number('limit', 50, 1, 500);
      const offset = number('offset', 0, 0, Number.MAX_SAFE_INTEGER);
      const year = number('year', null, 1800, 9999);
      const minRating = number('minRating', null, 0, 10, false);
      const sort = params.get('sort') ?? 'title';
      const order = params.get('order') ?? 'asc';
      if (
        !['title', 'year', 'rating', 'popularity'].includes(sort) ||
        !['asc', 'desc'].includes(order)
      ) {
        return send(400, { error: 'Invalid sort or order' });
      }
      const query = fold((params.get('q') ?? '').trim());
      const genre = fold(params.get('genre') ?? '');
      const language = fold(params.get('language') ?? '');
      const filtered = movies.filter(
        (movie) =>
          (!query ||
            [movie.title, movie.director, ...movie.cast].some((value) =>
              fold(value).includes(query),
            )) &&
          (!genre || movie.genres.some((value) => fold(value) === genre)) &&
          (!language || fold(movie.originalLanguage ?? '') === language) &&
          (year === null || movie.year === year) &&
          (minRating === null ||
            (movie.rating > 0 && movie.rating >= minRating)),
      );
      filtered.sort((a, b) => {
        const primary =
          sort === 'title'
            ? a.title.localeCompare(b.title, 'en')
            : (a[sort] ?? 0) - (b[sort] ?? 0);
        return (
          primary * (order === 'desc' ? -1 : 1) || a.id.localeCompare(b.id)
        );
      });
      return send(200, {
        movies: filtered.slice(offset, offset + limit),
        total: filtered.length,
        limit,
        offset,
      });
    } catch (error) {
      if (
        error instanceof RangeError ||
        error instanceof URIError ||
        error instanceof TypeError
      ) {
        return send(400, { error: error.message });
      }
      console.error(error);
      return send(500, { error: 'Internal server error' });
    }
  };
}

export async function startServer({
  host = process.env.HOST ?? '127.0.0.1',
  port = Number(process.env.PORT ?? 3000),
} = {}) {
  if (!Number.isInteger(port) || port < 0 || port > 65535)
    throw new Error('Invalid PORT');
  const catalog = JSON.parse(await readFile(catalogPath, 'utf8'));
  if (
    !catalog.movies?.length ||
    new Set(catalog.movies.map((movie) => movie.id)).size !==
      catalog.movies.length
  ) {
    throw new Error(
      'Missing movies or duplicate IDs; run npm --prefix api run setup',
    );
  }
  const server = createServer(createHandler(catalog));
  await new Promise((resolve, reject) => {
    server.once('error', reject);
    server.listen(port, host, resolve);
  });
  return server;
}

if (
  process.argv[1] &&
  resolve(process.argv[1]) === fileURLToPath(import.meta.url)
) {
  const server = await startServer();
  console.log(
    `Catalog API listening on http://${process.env.HOST ?? '127.0.0.1'}:${server.address().port}`,
  );
  for (const signal of ['SIGINT', 'SIGTERM'])
    process.on(signal, () => server.close());
}
