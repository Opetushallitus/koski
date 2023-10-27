import * as A from 'fp-ts/Array'
import * as O from 'fp-ts/Option'
import { flow, pipe } from 'fp-ts/lib/function'

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
