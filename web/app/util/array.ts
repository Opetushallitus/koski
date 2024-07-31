import * as A from 'fp-ts/Array'
import * as O from 'fp-ts/Option'
import * as Ord from 'fp-ts/Ord'
import { pipe } from 'fp-ts/lib/function'

export const intersects = <T>(as: T[], bs: T[]): boolean =>
  as.find((a) => bs.includes(a)) !== undefined

export const deleteAt =
  (i: number) =>
  <T>(ts: T[]): T[] =>
    pipe(
      ts,
      A.deleteAt(i),
      O.getOrElseW(() => [])
    )

export const updateAt =
  <T>(i: number, t: T) =>
  (ts: T[]): T[] =>
    pipe(
      ts,
      A.updateAt(i, t),
      O.getOrElseW(() => [])
    )

export const appendOptional =
  <T>(t: T) =>
  (ts: T[] | undefined): T[] =>
    ts ? [...ts, t] : [t]

export const valuesFirst = <T>(...ts: T[]) =>
  Ord.fromCompare((a: T, b: T) =>
    ts.includes(a) ? (ts.includes(b) ? 0 : -1) : ts.includes(b) ? 1 : 0
  )
