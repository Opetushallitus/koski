import * as E from 'fp-ts/Either'

export const tap = <T>(fn: (right: T) => void) =>
  E.map((a: T) => {
    fn(a)
    return a
  })

export const tapLeft = <T>(leftSideEffect: (left: T) => void) =>
  E.mapLeft((left: T) => {
    leftSideEffect(left)
    return left
  })

export const tapLeftP =
  <T>(leftSideEffect: (left: T) => void) =>
  async <A>(promise: Promise<E.Either<T, A>>): Promise<E.Either<T, A>> =>
    promise.then(tapLeft(leftSideEffect))
