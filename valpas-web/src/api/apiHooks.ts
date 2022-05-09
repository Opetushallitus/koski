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
import { pluck } from "../utils/objects"
import { ApiFailure, ApiResponse, ApiSuccess } from "./apiFetch"
import { isError, isSuccess, isSuccessAndFinished } from "./apiUtils"
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
 * Tekee peräkkäisiä asynkronisia kutsuja funktiolla `fetchFn`, joka saa parametrinsa joka iteraatiolla
 * kutsuttavalta `getNext`-funktiolta. Sekvenssi loppuu, kunnes hook unmountataan tai `getNext` palauttaa arvon
 * null. Hookille annetaan parametrina alkutila `initialState`, jonka `getNext` saa ensimmäisellä iteraatiolla.
 * getNext palauttaa tuplen, jonka sisältö on [fetchFn:lle annettavat parametrit, seuraavalle iteraation tila]
 * tai null, jos ei haluta enää tehdä uusia kutsuja.
 *
 * Hook palauttaa listan tehdyistä ja mahdollisesti käynnissä olevista kutsuista.
 * Kutsujan vastuu on yhdistää itse listasta saadut datat ja/tai virheet kutsupaikallaan.
 * 
 * Esimerkki:
  
  const numberFetches = useApiSequence(
    fetchPhoneNumberForName,
    ["Alice", "Bob", "Celestia"],
    useCallback(
      (names) => {
        const [nextName, ...rest] = names
        if (nextName) {
          const fetchParams: Parameters<typeof fetchPhoneNumberForName> = [nextName]
          return [fetchParams, rest]
        }
        return null
      },
      []
    ),
  )

  const numbers = useMemo(
    () => A.flatten(numberFetches.filter(isSuccess).map((r) => r.data)),
    [numberFetches]
  )
  
 *
 */
export const useApiSequence = <T, P extends any[], S>(
  fetchFn: (...params: P) => Promise<ApiResponse<T>>,
  initialState: S,
  getNext: (state: S) => [P, S] | null,
  cache?: ApiCache<T, P>
): ApiMethodState<T>[] => {
  const api = useApiMethod(fetchFn, cache)
  const doFetch = api.call
  const [results, setResults] = useSafeState<ApiMethodState<T>[]>([])

  useOnApiSuccess(api, (result) => {
    setResults([...results, result])
  })

  useOnApiError(api, (result) => {
    setResults([...results, result])
  })

  useEffect(() => {
    setResults([])
  }, [initialState, setResults])

  useEffect(() => {
    let state = initialState
    let cancelled = false

    const runNext = async () => {
      const next = getNext(state)
      if (next !== null && !cancelled) {
        const [params, nextState] = next
        await doFetch(...params)
        state = nextState
        setTimeout(runNext)
      }
    }

    runNext()

    return () => {
      cancelled = true
    }
  }, [doFetch, getNext, initialState])

  return results
}

/**
 * Get data from cache without ever triggering API calls.
 * The data is updated on both cache content and parameter changes.
 */
export const useCacheWithParams = <T, P extends any[]>(
  cache: ApiCache<T, P>,
  params?: P
) => {
  const [cacheChangeTrigger, setChangeTrigger] = useState(Symbol())

  useEffect(() => {
    return cache.addChangeListener(() => {
      setChangeTrigger(Symbol())
    })
  }, [cache])

  return useMemo(
    () =>
      pipe(
        O.fromNullable(params),
        O.chain(cache.get),
        O.map(pluck("data")),
        O.getOrElseW(() => null)
      ),
    [cache.get, params, cacheChangeTrigger] // eslint-disable-line react-hooks/exhaustive-deps
  )
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

export const useOnApiError = <T, P extends any[]>(
  hook: ApiMethodHook<T, P>,
  handler: (hook: ApiMethodStateError) => void
) => {
  const errorsRef = useRef(isError(hook) ? hook.errors : null)

  useEffect(() => {
    if (isError(hook)) {
      if (hook.errors !== errorsRef.current) {
        errorsRef.current = hook.errors
        handler(hook)
      }
    } else {
      errorsRef.current = null
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
