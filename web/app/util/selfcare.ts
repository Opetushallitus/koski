export const assertNever = (any: never): never => {
  throw new Error(
    `Reached an unintended code branch. Expected type is 'never' but actual value is ${typeof any}`
  )
}
