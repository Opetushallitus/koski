export type PatternMatcher<T, R> = {
  case: (when: (a: T) => boolean, then: (a: T) => R) => PatternMatcher<T, R>
  isClass: (
    classDef: { className: string },
    then: (a: T) => R
  ) => PatternMatcher<T, R>
  getOrNull: () => R | null
  get: () => R
}

export const match = <T, R = any>(value: T): PatternMatcher<T, R> => {
  let firstResult: R | undefined

  return {
    case(when, then) {
      if (firstResult === undefined && when(value)) {
        firstResult = then(value)
      }
      return this
    },
    isClass(classDef, then) {
      if (
        firstResult === undefined &&
        typeof value === 'object' &&
        (value as any).$class === classDef.className
      ) {
        firstResult = then(value)
      }
      return this
    },
    getOrNull() {
      return firstResult === undefined ? null : firstResult
    },
    get() {
      if (firstResult) return firstResult
      throw Error(`No matches for ${value}`)
    }
  }
}
