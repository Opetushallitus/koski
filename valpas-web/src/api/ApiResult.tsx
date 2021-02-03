import { pipe } from "fp-ts/lib/pipeable"
import * as O from "fp-ts/Option"
import React from "react"
import { ApiFailure } from "./apiFetch"
import {
  ApiMethodState,
  isError,
  isInitial,
  isLoading,
  isSuccessful,
} from "./apiHooks"

type ApiMappings<T, S> = {
  initial?: () => S
  loading?: () => S
  successful?: (data: T) => S
  error?: (error: ApiFailure) => S
  else?: () => S
}

export type ApiResultProps<T> = ApiMappings<T, React.ReactNode> & {
  state: ApiMethodState<T>
}

export const ApiResult = <T,>({
  state,
  ...mappings
}: ApiResultProps<T>): React.ReactElement => (
  <>{mapApiResult(state, mappings)}</>
)

export const mapApiResult = <T, S>(
  state: ApiMethodState<T>,
  mappings: ApiMappings<T, S>
): O.Option<S> => {
  if (isInitial(state)) {
    return pipe(
      O.fromNullable(mappings.initial),
      O.map((f) => f())
    )
  }
  if (isLoading(state)) {
    return pipe(
      O.fromNullable(mappings.loading),
      O.map((f) => f())
    )
  }
  if (isSuccessful(state)) {
    return pipe(
      O.fromNullable(mappings.successful),
      O.map((f) => f(state.data))
    )
  }
  if (isError(state)) {
    return pipe(
      O.fromNullable(mappings.error),
      O.map((f) => f(state))
    )
  }
  return pipe(
    O.fromNullable(mappings.else),
    O.map((f) => f())
  )
}
