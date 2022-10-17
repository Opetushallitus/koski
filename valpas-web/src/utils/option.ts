import { pipe } from "fp-ts/lib/function"
import * as O from "fp-ts/Option"

export const renderOption = <T>(
  o: O.Option<T>,
  render: (t: T) => JSX.Element
): JSX.Element =>
  pipe(
    o,
    O.map(render),
    O.getOrElse(() => null as unknown as JSX.Element)
  )
