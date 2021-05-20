export type FilterableValue = string | number | boolean | null | undefined

export const toFilterableString = (input: FilterableValue): string => {
  if (input === null || input == undefined) {
    return ""
  }
  return input.toString()
}
