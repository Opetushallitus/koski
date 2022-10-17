import { format } from "date-fns"
import { pipe } from "fp-ts/lib/function"
import * as O from "fp-ts/Option"
import { DATE_FORMAT } from "../utils/date"
import { pluck } from "../utils/objects"

export const hetulikeRegex =
  /[0123]\d[01]\d\d\d[-+A]\d\d\d[0123456789ABCDEFHJKLMNPRSTUVWXY]/g

export const parseHetulikes = (input: string): string[] =>
  pipe(input.toUpperCase().match(hetulikeRegex) || [], uniq)

const uniq = <T>(ts: T[]): T[] => Array.from(new Set(ts))

export const hetuToBirthday = (input: string = "") =>
  pipe(
    input.toUpperCase().match(/(\d\d)(\d\d)(\d\d)([+-A]).+/),
    O.fromNullable,
    O.map((tokens) => {
      const [d, m, y] = tokens.slice(1, 4).map((n) => parseInt(n, 10))
      const c = delimiterToCentury(tokens[4]!)
      return format(new Date(y! + c!, m! - 1, d), DATE_FORMAT)
    })
  )

const delimiterToCentury = (d: string): number | undefined =>
  pipe(
    {
      "+": 1800,
      "-": 1900,
      A: 2000,
    } as Record<string, number>,
    pluck(d)
  )
