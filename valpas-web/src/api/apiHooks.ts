import * as E from "fp-ts/Either"
import { pipe } from "fp-ts/lib/function"
import { useCallback, useEffect, useMemo, useState } from "react"
import { ApiFailure, ApiResponse, ApiSuccess } from "./apiFetch"
import { isInitial, isSuccess } from "./apiUtils"
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
  }, [JSON.stringify(params)]) // eslint-disable-line react-hooks/exhaustive-deps
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
} & ApiMethodState<T>

export const useApiMethod = <T, P extends any[]>(
  fetchFn: (...args: P) => Promise<ApiResponse<T>>,
  cache?: ApiCache<T, P>
): ApiMethodHook<T, P> => {
  const [state, setState] = useState<ApiMethodState<T>>({
    state: "initial",
  })

  const [mounted, setMounted] = useState(true)
  useEffect(() => () => setMounted(false), [])

  const safeSetState = useCallback(
    (newState: ApiMethodState<T>) => {
      if (mounted) {
        setState(newState)
      }
    },
    [mounted]
  )

  const call = useCallback(
    async (...args: P) => {
      safeSetState({ state: "loading" })
      cache?.map(args, (previous) =>
        safeSetState({ state: "reloading", ...previous })
      )
      pipe(
        await fetchFn(...args),
        E.map((result) => {
          safeSetState({
            state: "success",
            ...result,
          })
          cache?.set(args, result)
        }),
        E.mapLeft((error) =>
          safeSetState({
            state: "error",
            ...error,
          })
        )
      )
    },
    [cache, fetchFn, safeSetState]
  )

  const clear = useCallback(() => setState({ state: "initial" }), [setState])

  return useMemo(
    () => ({
      ...state,
      call,
      clear,
    }),
    [state, call, clear]
  )
}

export const useOnApiSuccess = <T, P extends any[]>(
  hook: ApiMethodHook<T, P>,
  handler: (hook: ApiMethodStateSuccess<T>) => void
) => {
  const [triggered, setTriggered] = useState(false)

  useEffect(() => {
    if (isSuccess(hook) && !triggered) {
      setTriggered(true)
      handler(hook)
    } else if (isInitial(hook) && triggered) {
      setTriggered(false)
    }
  }, [hook, handler, triggered])
}
