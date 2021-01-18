import * as E from "fp-ts/Either"
import * as O from "fp-ts/Option"
import { pipe } from "fp-ts/lib/function"

export const forNullableEither = <L, R>(
  e: E.Either<L, R> | null,
  fn: (value: R) => void
) => pipe(e ? O.fromEither(e) : O.none, O.map(fn))
