import * as E from "fp-ts/Either"
import { ApiError, ApiResponse } from "./apiFetch"
import {
  ApiMethodState,
  ApiMethodStateError,
  ApiMethodStateInitial,
  ApiMethodStateLoading,
  ApiMethodStateSuccess,
} from "./apiHooks"

export const isInitial = <T>(
  state: ApiMethodState<T>,
): state is ApiMethodStateInitial => state.state === "initial"

export const isLoading = <T>(
  state: ApiMethodState<T>,
): state is ApiMethodStateLoading => state.state === "loading"

export const isSuccess = <T>(
  state: ApiMethodState<T>,
): state is ApiMethodStateSuccess<T> =>
  state.state === "success" || state.state === "reloading"

export const isSuccessAndFinished = <T>(
  state: ApiMethodState<T>,
): state is ApiMethodStateSuccess<T> => state.state === "success"

export const isError = <T>(
  state: ApiMethodState<T>,
): state is ApiMethodStateError => state.state === "error"

export const mapInitial = <T, S>(
  state: ApiMethodState<T>,
  fn: () => S | null,
) => (isInitial(state) ? fn() : null)

export const mapLoading = <T, S>(
  state: ApiMethodState<T>,
  fn: () => S | null,
) => (isLoading(state) ? fn() : null)

export const mapSuccess = <T, S>(
  state: ApiMethodState<T>,
  fn: (data: T) => S | null,
) => (isSuccess(state) ? fn(state.data) : null)

export const mapError = <T, S>(
  state: ApiMethodState<T>,
  fn: (errors: ApiError[]) => S | null,
) => (isError(state) ? fn(state.errors) : null)

export const withRetries = async <T>(
  times: number,
  fn: () => Promise<ApiResponse<T>>,
): Promise<ApiResponse<T>> => {
  const response = await fn()
  if (E.isLeft(response) && times > 0) {
    await new Promise((r) => setTimeout(r, 100))
    return withRetries(times - 1, fn)
  }
  return response
}
