export const memoize = <A extends any[], T>(
  fn: (...a: A) => T,
  getKey: (...a: A) => string
) => {
  const cache: Record<string, T> = {}
  return (...args: A): T => {
    const key = getKey(...args)
    if (cache[key]) {
      return cache[key]
    }
    const result = fn(...args)
    cache[key] = result
    return result
  }
}
