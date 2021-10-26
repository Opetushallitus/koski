import * as A from "fp-ts/lib/Array"
import { pipe } from "fp-ts/lib/function"
import * as string from "fp-ts/string"

export const hetulikeRegex = /[0123]\d[01]\d\d\d[-+A]\d\d\d[0123456789ABCDEFHJKLMNPRSTUVWXY]/g

export const parseHetulikes = (input: string): string[] =>
  pipe(input.toUpperCase().match(hetulikeRegex) || [], A.uniq(string.Eq))
