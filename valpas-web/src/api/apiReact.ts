import * as E from "fp-ts/lib/Either"
import { pipe } from "fp-ts/lib/function"
import { useEffect, useState } from "react"
import { ApiError, ApiResponse, ApiSuccess } from "./apiFetch"

export type ApiLoading = null
export type ApiResponseState<T> = ApiLoading | ApiResponse<T>

export const useApiState = <T>() => useState<ApiResponseState<T>>(null)

export const useApiOnce = <T>(fetchFn: () => Promise<ApiResponse<T>>) => {
  const [result, setResult] = useState<ApiResponse<T> | ApiLoading>(null)
  useEffect(() => {
    fetchFn().then(setResult)
  }, [])
  return result
}

export const renderResponse = <T>(
  response: ApiResponse<T> | ApiLoading,
  mappings: {
    success?: (success: ApiSuccess<T>) => React.ReactNode
    error?: (error: ApiError) => React.ReactNode
    loading?: () => React.ReactNode
  }
): React.ReactNode | undefined => {
  if (response === null) {
    return mappings.loading?.()
  }
  return pipe(
    response,
    E.map((success) => mappings.success?.(success)),
    E.getOrElse((error: ApiError) => mappings.error?.(error))
  )
}
