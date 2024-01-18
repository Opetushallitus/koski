import * as E from "fp-ts/Either"
import { pipe } from "fp-ts/lib/function"
import * as O from "fp-ts/Option"
import { ApiFailure, ApiSuccess } from "./apiFetch"

export type ApiCache<T, S> = {
  contains: (key: S) => boolean
  get: (key: S) => O.Option<ApiSuccess<T>>
  getOnlyFresh: (key: S) => O.Option<ApiSuccess<T>>
  set: (key: S, value: ApiSuccess<T>) => void
  map: <U>(key: S, fn: (value: ApiSuccess<T>) => U) => O.Option<U>
  clear: (key: S) => void
  clearAll: () => void
  addChangeListener: (listener: ApiCacheChangeListener) => () => void
}

export type ApiCacheChangeListener = () => void

export const createPreferLocalCache = <T, S extends any[]>(
  _fn: (...args: S) => Promise<E.Either<ApiFailure, ApiSuccess<T>>>,
): ApiCache<T, S> => {
  const cachedValues: Record<string, ApiSuccess<T>> = {}
  const keyToString = (key: S) => JSON.stringify(key)
  const get = (key: S) => O.fromNullable(cachedValues[keyToString(key)])
  let listeners: ApiCacheChangeListener[] = []
  const notifyListeners = () => {
    listeners.forEach((l) => l())
  }

  return {
    contains(key) {
      return O.isSome(get(key))
    },
    get,
    getOnlyFresh: get,
    set(key, value) {
      cachedValues[keyToString(key)] = value
      notifyListeners()
    },
    map(key, fn) {
      return pipe(cachedValues[keyToString(key)], O.fromNullable, O.map(fn))
    },
    clear(key) {
      delete cachedValues[keyToString(key)]
      notifyListeners()
    },
    clearAll() {
      Object.keys(cachedValues).forEach((key) => {
        delete cachedValues[key]
      })
      notifyListeners()
    },
    addChangeListener(listener) {
      listeners.push(listener)
      return () => {
        listeners = listeners.filter((l) => l !== listener)
      }
    },
  }
}

export const createLocalThenApiCache = <T, S extends any[]>(
  fn: (...args: S) => Promise<E.Either<ApiFailure, ApiSuccess<T>>>,
): ApiCache<T, S> => ({
  ...createPreferLocalCache(fn),
  getOnlyFresh: (_key) => O.none, // Pakottaa kutsun backendille, vaikka data löytyisikin muistista
})
