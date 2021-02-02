import * as E from "fp-ts/Either"
import { pipe } from "fp-ts/lib/function"
import { useCallback, useEffect, useState } from "react"
import { ApiFailure, ApiResponse, ApiSuccess } from "./apiFetch"

export type ApiLoading = null
export type ApiResponseState<T> = ApiLoading | ApiResponse<T>

export const useApiState = <T>() => useState<ApiResponseState<T>>(null)

/**
 * Triggers API call once on component mount
 * @param fetchFn
 */
export const useApiOnce = <T>(fetchFn: () => Promise<ApiResponse<T>>) =>
  useApiWithParams(fetchFn, [])

/**
 * Triggers API call once on component mount or when parameters change
 * @param fetchFn
 *
 * Example:
 *
 * // function fetchOppija(oppijaOid: string): ApiResponse;
 *
 * const oppija = useApiWithParams(fetchOppija, [oppijaOid])
 */
export const useApiWithParams = <T, P extends any[]>(
  fetchFn: (...fetchFnParams: P) => Promise<ApiResponse<T>>,
  params: P
) => {
  const api = useApiMethod(fetchFn)
  useEffect(() => {
    api.call(...params)
  }, params)
  return api
}

/**
 *
 */
export type ApiMethodState<T> =
  | ApiMethodStateInitial
  | ApiMethodStateLoading
  | ApiMethodStateSuccess<T>
  | ApiMethodStateError

export type ApiMethodStateInitial = { state: "initial" }
export type ApiMethodStateLoading = { state: "loading" }
export type ApiMethodStateSuccess<T> = { state: "success" } & ApiSuccess<T>
export type ApiMethodStateError = { state: "error" } & ApiFailure

export type ApiMethodHook<T, P extends any[]> = {
  call: (...args: P) => Promise<void>
  clear: () => void
  flatMap: <R>(fn: (data: T) => R) => R | undefined
} & ApiMethodState<T>

export const useApiMethod = <T, P extends any[]>(
  fetchFn: (...args: P) => Promise<ApiResponse<T>>
): ApiMethodHook<T, P> => {
  const [state, setState] = useState<ApiMethodState<T>>({
    state: "initial",
  })

  const call = useCallback(
    async (...args: P) => {
      setState({ state: "loading" })
      pipe(
        await fetchFn(...args),
        E.map((result) =>
          setState({
            state: "success",
            ...result,
          })
        ),
        E.mapLeft((error) =>
          setState({
            state: "error",
            ...error,
          })
        )
      )
    },
    [setState, state]
  )

  const clear = useCallback(() => setState({ state: "initial" }), [
    setState,
    state,
  ])

  return {
    ...state,
    call,
    clear,
    flatMap(fn) {
      return state.state === "success" ? fn(state.data) : undefined
    },
  }
}

export const isInitial = <T>(
  state: ApiMethodState<T>
): state is ApiMethodStateInitial => state.state === "initial"

export const isLoading = <T>(
  state: ApiMethodState<T>
): state is ApiMethodStateLoading => state.state === "loading"

export const isSuccessful = <T>(
  state: ApiMethodState<T>
): state is ApiMethodStateSuccess<T> => state.state === "success"

export const isError = <T>(
  state: ApiMethodState<T>
): state is ApiMethodStateError => state.state === "error"
