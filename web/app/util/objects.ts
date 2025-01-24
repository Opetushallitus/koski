export type EmptyObject = { _?: never }

export const entries = <T>(obj: Record<string, T>): Array<[string, T]> =>
  Object.entries(obj)

export const withoutNullValues = <T extends object>(obj: T): Partial<T> =>
  Object.fromEntries(
    Object.entries(obj).filter(
      ([_key, value]) => value !== null && value !== undefined
    )
  ) as Partial<T>

export const isEmptyModelObject = (obj: any): boolean => {
  if (obj === undefined || obj === null) {
    return true
  }
  if (
    typeof obj === 'string' ||
    typeof obj === 'number' ||
    typeof obj === 'boolean'
  ) {
    return false
  }
  if (typeof obj === 'object') {
    if (Array.isArray(obj) && obj.length > 0) {
      return false
    }
    for (const [key, value] of Object.entries(obj)) {
      if (key === '$class') {
        continue
      }
      if (!isEmptyModelObject(value)) {
        return false
      }
    }
    return true
  }

  return true
}
