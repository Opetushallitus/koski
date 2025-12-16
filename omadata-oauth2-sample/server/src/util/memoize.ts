export const memoize = <A extends unknown[], T>(
  fn: (...a: A) => Promise<T>,
  getKey: (...a: A) => string
) => {
  const cache: Record<string, T> = {}
  return async (...args: A): Promise<T> => {
    const key = getKey(...args)
    if (cache[key]) {
      return cache[key]
    }
    const result = await fn(...args)
    if (result !== undefined && result !== null) {
      cache[key] = result
    }
    return result
  }
}
