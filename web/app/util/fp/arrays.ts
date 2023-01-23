import * as A from 'fp-ts/Array'
import * as NEA from 'fp-ts/NonEmptyArray'
import { Eq } from 'fp-ts/lib/Eq'
import { pipe, Predicate } from 'fp-ts/lib/function'
import { NonEmptyArray } from 'fp-ts/lib/NonEmptyArray'
import * as O from 'fp-ts/Option'
import { deepEqual } from './objects'

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

export const nonFalsy = <T>(a: T | undefined | null | false): a is T =>
  a !== undefined && a !== null && a !== false

export const toggleItemExistence = <T>(arr: T[], item: T): T[] =>
  arr.includes(item) ? arr.filter((a) => a !== item) : [...arr, item]

export const nonEmptyEvery = <T>(
  arr: T[],
  cond: (t: T) => boolean
): arr is NonEmptyArray<T> => A.isNonEmpty(arr) && arr.every(cond)

export const asArray = <T>(arrayOrSingular: T | T[]): T[] =>
  Array.isArray(arrayOrSingular) ? arrayOrSingular : [arrayOrSingular]

export const nullableJoinToString =
  (delimiter: string) =>
  (arr: Array<string | null | undefined>): string | null => {
    const definedStrings = arr.filter(nonNull)
    return A.isNonEmpty(definedStrings) ? definedStrings.join(delimiter) : null
  }

export const intersects =
  <T>(eq: Eq<T>) =>
  (xs: T[]) =>
  (ys: T[]): boolean =>
    pipe(A.intersection(eq)(xs)(ys), A.isNonEmpty)

export const arraysEqual = (a: any[], b: any[]): boolean => {
  if (a.length !== b.length) {
    return false
  }
  return !a.find((aValue, index) => !deepEqual(aValue, b[index]))
}

export const append =
  <T>(a: T) =>
  (as?: T[]): NEA.NonEmptyArray<T> =>
    [...(as || []), a] as any as NEA.NonEmptyArray<T>

export const ensureArray = <T>(a: T | T[]): NEA.NonEmptyArray<T> =>
  (Array.isArray(a) ? a : [a]) as NEA.NonEmptyArray<T>

export const initialsAndLast = <T>(as: NEA.NonEmptyArray<T>): [T[], T] => [
  as.slice(0, -1),
  as[as.length - 1]
]

export const deleteAt = <T>(as: T[], index: number): T[] =>
  pipe(
    as,
    A.deleteAt(index),
    O.fold(
      () => as,
      (as) => as
    )
  )

export const mapTimes = <T>(count: number, fn: (index: number) => T): T[] =>
  new Array(count).fill(undefined).map((_, i) => fn(i))
