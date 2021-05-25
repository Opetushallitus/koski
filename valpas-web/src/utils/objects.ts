import * as A from "fp-ts/Array"

export type ObjectEntry<T> = [string, T]

export const isEntry = (arr: string[]): arr is [string, string] =>
  arr.length === 2

export const objectEntry = <T>(key: string, value: T): ObjectEntry<T> => [
  key,
  value,
]

export const fromEntries = <T>(
  entries: Array<ObjectEntry<T>>
): Record<string, T> =>
  entries.reduce(
    (obj, entry) => ({
      ...obj,
      [entry[0]]: entry[1],
    }),
    {}
  )

export const isEmptyObject = <T extends object>(obj: T): boolean =>
  Object.entries(obj).length === 0

export const removeFalsyValues = <T extends object>(obj: T): Partial<T> => {
  const partial: Partial<T> = {}
  for (const key in obj) {
    if (obj[key]) {
      partial[key] = obj[key]
    }
  }
  return partial
}

export const pluck = <T extends object, K extends keyof T>(key: K) => (
  obj: T
): T[K] => obj[key]

export const pick = <T extends object, K extends keyof T>(key: K) =>
  A.map<T, T[K]>(pluck(key))
