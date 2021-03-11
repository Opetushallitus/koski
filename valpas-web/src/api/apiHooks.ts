import * as E from "fp-ts/Either"
import { pipe } from "fp-ts/lib/function"
import { useCallback, useEffect, useState } from "react"
import { ApiFailure, ApiResponse, ApiSuccess } from "./apiFetch"
import { ApiCache } from "./cache"

export type ApiLoading = null
export type ApiResponseState<T> = ApiLoading | ApiResponse<T>

export const useApiState = <T>() => useState<ApiResponseState<T>>(null)

/**
 * Triggers API call once on component mount
 * @param fetchFn
 */
export const useApiOnce = <T>(
  fetchFn: () => Promise<ApiResponse<T>>,
  cache?: ApiCache<T, []>
) => useApiWithParams(fetchFn, [], cache)

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
  params?: P,
  cache?: ApiCache<T, P>
) => {
  const api = useApiMethod(fetchFn, cache)
  useEffect(() => {
    if (params) {
      api.call(...params)
    } else {
      api.clear()
    }
  }, params)
  return api
}

/**
 *
 */
export type ApiMethodState<T> =
  | ApiMethodStateInitial
  | ApiMethodStateLoading
  | ApiMethodStateReloading<T>
  | ApiMethodStateSuccess<T>
  | ApiMethodStateError

export type ApiMethodStateInitial = { state: "initial" }
export type ApiMethodStateLoading = { state: "loading" }
export type ApiMethodStateReloading<T> = { state: "reloading" } & ApiSuccess<T>
export type ApiMethodStateSuccess<T> = { state: "success" } & ApiSuccess<T>
export type ApiMethodStateError = { state: "error" } & ApiFailure

export type ApiMethodHook<T, P extends any[]> = {
  call: (...args: P) => Promise<void>
  clear: () => void
  flatMap: <R>(fn: (data: T) => R) => R | undefined
} & ApiMethodState<T>

export const useApiMethod = <T, P extends any[]>(
  fetchFn: (...args: P) => Promise<ApiResponse<T>>,
  cache?: ApiCache<T, P>
): ApiMethodHook<T, P> => {
  const [state, setState] = useState<ApiMethodState<T>>({
    state: "initial",
  })

  const call = useCallback(
    async (...args: P) => {
      setState({ state: "loading" })
      cache?.map(args, (previous) =>
        setState({ state: "reloading", ...previous })
      )
      pipe(
        await fetchFn(...args),
        E.map((result) => {
          setState({
            state: "success",
            ...result,
          })
          cache?.set(args, result)
        }),
        E.mapLeft((error) =>
          setState({
            state: "error",
            ...error,
          })
        )
      )
    },
    [fetchFn, setState, state]
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
