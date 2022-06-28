/**
 * A type guard function to to filter null, false and undefined
 * @param x Param
 * @returns True, if x is not false, null or undefined
 */
export function filterFalsy<T>(
  x: T | false | undefined
): x is Exclude<T, null | false | undefined> {
  return x !== false && x !== undefined && x !== null
}
