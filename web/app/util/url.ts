import { isEmptyObject } from './fp/objects'

export const queryString = (query: Record<string, string>) =>
  isEmptyObject(query)
    ? ''
    : '?' +
      Object.entries(query)
        .map(
          ([key, value]) =>
            `${encodeURIComponent(key)}=${encodeURIComponent(value)}`
        )
        .join('&')
