export type FilterableValue = string | number | null | undefined

export const toFilterableString = (input: FilterableValue): string => {
  if (input === null || input == undefined) {
    return ""
  }
  return input.toString()
}
