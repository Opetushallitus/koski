export const withoutNullValues = <T extends object>(obj: T): Partial<T> =>
  Object.fromEntries(
    Object.entries(obj).filter(
      ([_key, value]) => value !== null && value !== undefined
    )
  ) as Partial<T>

export const isEmptyModelObject = (obj: any): Boolean => {
  if (obj === undefined || obj === null) {
    return true
  }
  if ('length' in obj && obj.length > 0) {
    return false
  }
  if (typeof obj === 'object') {
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
