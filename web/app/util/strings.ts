export const capitalize = (str: string): string =>
  str.slice(0, 1).toUpperCase() + str.slice(1)

export const uncapitalize = (str: string): string =>
  str.slice(0, 1).toLowerCase() + str.slice(1)

export const textSearch = (query: string): ((text: string) => boolean) => {
  const pattern = query.toLowerCase().trim().split(' ').join('.*')
  return (text) => !!text.toLowerCase().match(new RegExp(pattern))
}

export const coerceForSort = (value: string): string =>
  value
    .split(' ')
    .map((x) => {
      const n = parseFloat(x.replace(',', '.'))
      return Number.isFinite(n) ? x.padStart(16, '0') : x
    })
    .join(' ')
