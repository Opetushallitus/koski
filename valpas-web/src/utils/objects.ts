export const isEntry = (arr: string[]): arr is [string, string] =>
  arr.length === 2

export const fromEntries = <T>(entries: [string, T][]): Record<string, T> =>
  entries.reduce(
    (obj, entry) => ({
      ...obj,
      [entry[0]]: entry[1],
    }),
    {}
  )
