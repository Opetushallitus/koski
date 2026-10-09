import { fromEntries, isEmptyObject, ObjectEntry } from './fp/objects'
import { siirry } from './router'

export type LocationQueryIn = Record<string, string | number | boolean | null>
export type LocationQueryOut = Record<string, string>

export const queryString = (query: LocationQueryIn) =>
  isEmptyObject(query)
    ? ''
    : '?' +
      Object.entries(query)
        .filter(([_, value]) => value !== undefined)
        .map(([key, value]) =>
          value !== null
            ? `${encodeURIComponent(key)}=${encodeURIComponent(value)}`
            : ''
        )
        .join('&')

export const parseQuery = (query: string): LocationQueryOut => {
  const entries = query
    .match(/^(.*?)\?(.*)/)?.[2]
    ?.split('&')
    ?.map((pair) => pair.split('='))
    ?.map((pair) => pair.map(decodeURIComponent) as ObjectEntry<string>)
  return entries ? fromEntries(entries) : {}
}

export const updateQuery =
  (query: string) =>
  (params: LocationQueryIn): string =>
    queryString({
      ...parseQuery(query),
      ...params
    })

// Luetaan nykyinen osoite kutsuhetkellä (ei moduulin latautuessa), jotta
// asiakaspuolen navigoinnissa (pushLocation) parametrit yhdistyvät ajantasaiseen
// osoitteeseen eikä alkuperäiseen lataushetken osoitteeseen.
export const currentQueryWith = (params: LocationQueryIn): string =>
  updateQuery(window.location.href)(params)

export const goto = (href: string) => window.location.assign(href)

// Asiakaspuolen navigointi lataamatta sivua uudelleen.
export const pushLocation = (href: string): void => {
  siirry(href)
}
