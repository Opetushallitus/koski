type NonFalsy<T> = T extends false | "" ? never : T

/**
 * A type guard function to to filter null, falsy values and undefined
 * @param x Param
 * @returns True, if x is not falsy, null or undefined
 */
export function filterFalsy<T>(
  x: T | "" | false | undefined | null,
): x is NonNullable<T> & NonFalsy<T> {
  if (typeof x === "string" && x === "") {
    return false
  }
  return x !== undefined && x !== null && x !== false
}
