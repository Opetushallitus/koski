import * as A from "fp-ts/Array"
import { pipe } from "fp-ts/lib/function"
import { useMemo } from "react"

const randomChar = (chars: string) => (): string =>
  chars[Math.floor(Math.random() * chars.length)]!

const generatePassword = (length: number): string =>
  pipe(
    A.makeBy(
      length,
      randomChar("2345679ABCDEFHJKLMNPRSTUVWXYabcdefghjkmnpqrtuvwxy"),
    ),
    (a) => a.join(""),
  )

export const usePassword = () => {
  return useMemo(() => generatePassword(10), [])
}
