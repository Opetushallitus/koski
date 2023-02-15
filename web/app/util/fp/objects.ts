import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as O from 'fp-ts/Option'
import { arraysEqual } from './arrays'

export type ObjectEntry<T> = [string, T]

export const isEntry = (arr: string[]): arr is [string, string] =>
  arr.length === 2

export const objectEntry = <T>(key: string, value: T): ObjectEntry<T> => [
  key,
  value
]

export const fromEntries = <T>(
  entries: Array<ObjectEntry<T>>
): Record<string, T> =>
  entries.reduce(
    (obj, entry) => ({
      ...obj,
      [entry[0]]: entry[1]
    }),
    {}
  )

export const isEmptyObject = <T extends object>(obj: T): boolean =>
  Object.entries(obj).length === 0

export const isSingularObject = <T extends object>(obj: T): boolean =>
  Object.entries(obj).length === 1

export const removeFalsyValues = <T extends object>(obj: T): Partial<T> => {
  const partial: Partial<T> = {}
  for (const key in obj) {
    if (obj[key]) {
      partial[key] = obj[key]
    }
  }
  return partial
}

export const pluck =
  <T extends object, K extends keyof T>(key: K) =>
  (obj: T): T[K] =>
    obj[key]

export const pick = <T extends object, K extends keyof T>(key: K) =>
  A.map<T, T[K]>(pluck(key))

export const parseJson = <T>(json: string): O.Option<T> => {
  try {
    return O.some(JSON.parse(json))
  } catch (_e) {
    return O.none
  }
}

export const isObjectWithProp =
  <K extends string, V>(key: K, isTypeOf: (a: any) => a is V) =>
  (a: any): a is Record<K, V> =>
    typeof a === 'object' && a !== undefined && isTypeOf(a[key])

export const mapRecordToArray =
  <K extends string | number | symbol, T, S>(f: (value: T, key: K) => S) =>
  (obj: Record<K, T>): S[] =>
    Object.entries(obj).map(([key, value]) => f(value as T, key as K))

export const mapRecordValues =
  <K extends string | number | symbol, T, S>(f: (value: T, key: K) => S) =>
  (obj: Record<K, T>): Record<K, S> =>
    fromEntries(
      Object.entries(obj).map(([key, value]) => [key, f(value as T, key as K)])
    ) as Record<K, S>

export const mapObjectValues =
  <T extends { [K in keyof T]: T[K] }>(f: (value: T, key: keyof T) => T) =>
  (obj: T): T =>
    fromEntries(
      Object.entries(obj).map(([key, value]) => [
        key,
        f(value as T, key as keyof T)
      ])
    ) as T

export const deepEqual = (a: any, b: any): boolean => {
  if (a === b) {
    return true
  }
  if (Array.isArray(a) && Array.isArray(b)) {
    return arraysEqual(a, b)
  }
  if (typeof a === 'object' && typeof b === 'object') {
    if (Object.keys(a).length !== Object.keys(b).length) return false
    for (const [key, aValue] of Object.entries(a)) {
      if (!deepEqual(aValue, b[key])) return false
    }
    return true
  }
  return false
}

export const flattenObj = <T>(obj: Record<string, T[]>): T[] =>
  Object.values(obj).flat()

export const filterObjByKey =
  <T extends object>(fn: (key: keyof T) => boolean) =>
  (obj: T): Partial<T> =>
    pipe(
      Object.entries(obj),
      A.filter(([key]) => fn(key as keyof T)),
      (as) => fromEntries(as) as Partial<T>
    )
