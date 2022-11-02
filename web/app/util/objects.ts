export const withoutNullValues = <T extends object>(obj: T): Partial<T> =>
  Object.fromEntries(
    Object.entries(obj).filter(
      ([_key, value]) => value !== null && value !== undefined
    )
  ) as Partial<T>
