import * as A from "fp-ts/Array"
import { pipe } from "fp-ts/lib/function"
import { NonEmptyArray } from "fp-ts/lib/NonEmptyArray"
import * as O from "fp-ts/Option"

export const update = <T>(arr: T[], index: number, value: T): T[] =>
  pipe(
    A.updateAt(index, value)(arr),
    O.getOrElse(() => arr)
  )

export const nonNull = <T>(a: T | undefined | null): a is T =>
  a !== undefined && a !== null

export const toggleItemExistence = <T>(arr: T[], item: T): T[] =>
  arr.includes(item) ? arr.filter((a) => a !== item) : [...arr, item]

export const nonEmptyEvery = <T>(
  arr: T[],
  cond: (t: T) => boolean
): arr is NonEmptyArray<T> => A.isNonEmpty(arr) && arr.every(cond)
