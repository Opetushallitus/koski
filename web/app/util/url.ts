import { fromEntries, isEmptyObject, ObjectEntry } from './fp/objects'

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

// Tapahtuma, jolla pushLocation ilmoittaa osoitteen muuttuneen ilman koko sivun
// uudelleenlatausta. useSearchParam kuuntelee tätä (ja popstatea).
export const LOCATION_CHANGE_EVENT = 'locationchange'

// Asiakaspuolen navigointi: päivitä osoite selaimen historiaan lataamatta sivua
// uudelleen ja ilmoita muutoksesta osoitetta lukeville hookeille.
export const pushLocation = (href: string): void => {
  window.history.pushState({}, '', href)
  window.dispatchEvent(new Event(LOCATION_CHANGE_EVENT))
}
