import { pipe } from "fp-ts/lib/function"

export const hetulikeRegex = /[0123]\d[01]\d\d\d[-+A]\d\d\d[0123456789ABCDEFHJKLMNPRSTUVWXY]/g

export const parseHetulikes = (input: string): string[] =>
  pipe(input.toUpperCase().match(hetulikeRegex) || [], uniq)

const uniq = <T>(ts: T[]): T[] => Array.from(new Set(ts))
