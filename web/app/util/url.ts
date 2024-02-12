import { fromEntries, isEmptyObject, ObjectEntry } from './fp/objects'

export type LocationQueryIn = Record<string, string | number | boolean | null>
export type LocationQueryOut = Record<string, string>

export const queryString = (query: LocationQueryIn) =>
  isEmptyObject(query)
    ? ''
    : '?' +
      Object.entries(query)
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

export const currentQueryWith = updateQuery(window.location.href)

export const goto = (href: string) => window.location.assign(href)
