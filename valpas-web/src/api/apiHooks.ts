import * as E from "fp-ts/lib/Either"
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
export const useApiOnce = <T>(fetchFn: () => Promise<ApiResponse<T>>) => {
  const api = useApiMethod(fetchFn)
  useEffect(() => {
    api.call()
  }, [])
  return api
}

/**
 *
 */
export type ApiMethodState<T> =
  | { state: "initial" }
  | { state: "loading" }
  | ({ state: "success" } & ApiSuccess<T>)
  | ({ state: "error" } & ApiFailure)

export type ApiMethodHook<T, P extends any[]> = {
  call: (...args: P) => Promise<void>
  clear: () => void
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
  }
}
