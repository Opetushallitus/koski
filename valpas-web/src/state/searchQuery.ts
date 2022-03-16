import * as A from "fp-ts/Array"
import { pipe } from "fp-ts/lib/function"
import { nonNull } from "../utils/arrays"
import { fromEntries } from "../utils/objects"

export type SearchQueryEntry = [string, string]

const getQuery = () => window.location.search.replace(/^\?/, "")

const split = (query: string): string[] => query.split("&")

const parseToken = (token: string): SearchQueryEntry | null => {
  const match = token.match(/^(\w+)(=)?(.+)?$/)
  if (match) {
    const [_match, key, _eq, value] = match
    return [key!, value === undefined ? "true" : decodeURIComponent(value)]
  } else {
    return null
  }
}

export const getSearchQueryEntries = () =>
  pipe(getQuery(), split, A.map(parseToken), A.filter(nonNull))

export const getSearchQueryMap = () =>
  pipe(getSearchQueryEntries(), fromEntries)
