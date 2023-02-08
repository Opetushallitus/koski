export const uncapitalize = (str: string): string =>
  str.slice(0, 1).toLowerCase() + str.slice(1)

export const textSearch = (query: string): ((text: string) => boolean) => {
  const pattern = query.toLowerCase().trim().split(' ').join('.*')
  return (text) => !!text.toLowerCase().match(new RegExp(pattern))
}
