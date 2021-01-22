import * as A from "fp-ts/Array"
import * as O from "fp-ts/Option"
import { pipe } from "fp-ts/lib/function"

export const update = <T>(arr: T[], index: number, value: T): T[] =>
  pipe(
    A.updateAt(index, value)(arr),
    O.getOrElse(() => arr)
  )
