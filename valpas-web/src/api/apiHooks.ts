import * as E from "fp-ts/Either"
import { pipe } from "fp-ts/lib/function"
import * as O from "fp-ts/Option"
import {
  Dispatch,
  SetStateAction,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react"
import { useSafeState } from "../state/useSafeState"
import { ApiFailure, ApiResponse, ApiSuccess } from "./apiFetch"
import { isSuccess, isSuccessAndFinished } from "./apiUtils"
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
  call: (...args: P) => Promise<ApiResponse<T>>
  clear: () => void
} & ApiMethodState<T>

export const useApiMethod = <T, P extends any[]>(
  fetchFn: (...args: P) => Promise<ApiResponse<T>>,
  cache?: ApiCache<T, P>
): ApiMethodHook<T, P> => {
  const [state, setState] = useSafeState<ApiMethodState<T>>({
    state: "initial",
  })

  const call = useCallback(
    async (...args: P) => {
      const fresh = cache?.getOnlyFresh(args) || O.none
      if (O.isSome(fresh)) {
        setState({ state: "success", ...fresh.value })
        return E.right(fresh.value)
      }

      setState({ state: "loading" })
      cache?.map(args, (previous) =>
        setState({ state: "reloading", ...previous })
      )
      return pipe(
        await fetchFn(...args),
        E.map((result) => {
          setState({
            state: "success",
            ...result,
          })
          cache?.set(args, result)
          return result
        }),
        E.mapLeft((error) => {
          setState({
            state: "error",
            ...error,
          })
          return error
        })
      )
    },
    [cache, fetchFn, setState]
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
  const dataRef = useRef(isSuccess(hook) ? hook.data : null)

  useEffect(() => {
    if (isSuccessAndFinished(hook)) {
      if (hook.data !== dataRef.current) {
        dataRef.current = hook.data
        handler(hook)
      }
    } else {
      dataRef.current = null
    }
  }, [hook, handler])
}

export const useLocalDataCopy = <T, P extends any[]>(
  hook: ApiMethodHook<T, P>
): [T | null, Dispatch<SetStateAction<T | null>>] => {
  const [localData, setLocalData] = useSafeState<T | null>(null)
  useOnApiSuccess(hook, (o) => {
    setLocalData(o.data)
  })
  return [localData, setLocalData]
}
