import * as E from 'fp-ts/Either'
import { identity, pipe } from 'fp-ts/lib/function'
import * as TE from 'fp-ts/TaskEither'
import { ApiFailure, ApiResponse, ApiSuccess } from '../../api-fetch'

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

export const taskifyApiCall =
  <T, A extends any[]>(
    call: (...a: A) => Promise<E.Either<ApiFailure, ApiSuccess<T>>>
  ) =>
  (...args: A): TE.TaskEither<ApiFailure, ApiSuccess<T>> =>
    pipe(
      TE.tryCatchK(
        call,
        (error) =>
          ({
            errors: [{ messageKey: 'reject', messageData: error }]
          } as ApiFailure)
      )(...args),
      TE.chain(TE.fromEither)
    )
