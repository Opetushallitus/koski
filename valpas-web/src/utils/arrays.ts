import * as A from "fp-ts/Array"
import { pipe, Predicate } from "fp-ts/lib/function"
import { NonEmptyArray } from "fp-ts/lib/NonEmptyArray"
import * as O from "fp-ts/Option"

export const update = <T>(arr: T[], index: number, value: T): T[] =>
  pipe(
    A.updateAt(index, value)(arr),
    O.getOrElse(() => arr)
  )

export const upsert = <T>(
  arr: T[],
  predicate: Predicate<T>,
  value: T
): NonEmptyArray<T> => {
  const index = arr.findIndex(predicate)
  return index >= 0
    ? (A.unsafeUpdateAt(index, value, arr) as NonEmptyArray<T>)
    : A.append(value)(arr)
}

export const nonNull = <T>(a: T | undefined | null): a is T =>
  a !== undefined && a !== null

export const toggleItemExistence = <T>(arr: T[], item: T): T[] =>
  arr.includes(item) ? arr.filter((a) => a !== item) : [...arr, item]

export const nonEmptyEvery = <T>(
  arr: T[],
  cond: (t: T) => boolean
): arr is NonEmptyArray<T> => A.isNonEmpty(arr) && arr.every(cond)

export const asArray = <T>(arrayOrSingular: T | T[]): T[] =>
  Array.isArray(arrayOrSingular) ? arrayOrSingular : [arrayOrSingular]

export const joinToString = (
  arr: Array<string | null | undefined>
): string | null => {
  const definedStrings = arr.filter(nonNull)
  return A.isNonEmpty(definedStrings) ? definedStrings.join(" ") : null
}
