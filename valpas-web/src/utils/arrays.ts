import * as A from "fp-ts/Array"
import { pipe } from "fp-ts/lib/function"
import * as O from "fp-ts/Option"

export const update = <T>(arr: T[], index: number, value: T): T[] =>
  pipe(
    A.updateAt(index, value)(arr),
    O.getOrElse(() => arr)
  )
