export type CompareValue = string | number | undefined
export type CompareFn = (a: CompareValue, b: CompareValue) => number

export const ascending = (fn: CompareFn): CompareFn => fn
export const descending = (fn: CompareFn): CompareFn => (a, b) => 0 - fn(a, b)

export const compareStrings: CompareFn = (a, b) => {
  if (a === b) {
    return 0
  }
  if (a === undefined) {
    return -1
  }
  if (b === undefined) {
    return 1
  }
  return a.toString().localeCompare(b.toString())
}
