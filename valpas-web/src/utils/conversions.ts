export type FilterableNonNullValue = string | number | boolean
export type FilterableValue = FilterableNonNullValue | null | undefined

export const toFilterableString = (input: FilterableValue): string => {
  if (input === null || input === undefined) {
    return ""
  }
  return input.toString()
}
