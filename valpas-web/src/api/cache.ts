import * as E from "fp-ts/Either"
import { pipe } from "fp-ts/lib/function"
import * as O from "fp-ts/Option"
import { ApiFailure, ApiSuccess } from "./apiFetch"

export type ApiCache<T, S> = {
  get: (key: S) => O.Option<ApiSuccess<T>>
  getOnlyFresh: (key: S) => O.Option<ApiSuccess<T>>
  set: (key: S, value: ApiSuccess<T>) => void
  map: <U>(key: S, fn: (value: ApiSuccess<T>) => U) => O.Option<U>
  clear: (key: S) => void
}

export const createPreferLocalCache = <T, S extends any[]>(
  _fn: (...args: S) => Promise<E.Either<ApiFailure, ApiSuccess<T>>>
): ApiCache<T, S> => {
  let cachedValues: Record<string, ApiSuccess<T>> = {}
  const keyToString = (key: S) => JSON.stringify(key)
  const get = (key: S) => O.fromNullable(cachedValues[keyToString(key)])

  return {
    get,
    getOnlyFresh: get,
    set(key, value) {
      cachedValues[keyToString(key)] = value
    },
    map(key, fn) {
      return pipe(cachedValues[keyToString(key)], O.fromNullable, O.map(fn))
    },
    clear(key) {
      delete cachedValues[keyToString(key)]
    },
  }
}

export const createLocalThenApiCache = <T, S extends any[]>(
  fn: (...args: S) => Promise<E.Either<ApiFailure, ApiSuccess<T>>>
): ApiCache<T, S> => ({
  ...createPreferLocalCache(fn),
  getOnlyFresh: (_key) => O.none, // Pakottaa kutsun backendille, vaikka data löytyisikin muistista
})
