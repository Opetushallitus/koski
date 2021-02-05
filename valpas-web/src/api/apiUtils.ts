import { ApiError } from "./apiFetch"
import {
  ApiMethodState,
  ApiMethodStateError,
  ApiMethodStateInitial,
  ApiMethodStateLoading,
  ApiMethodStateSuccess,
} from "./apiHooks"

export const isInitial = <T>(
  state: ApiMethodState<T>
): state is ApiMethodStateInitial => state.state === "initial"

export const isLoading = <T>(
  state: ApiMethodState<T>
): state is ApiMethodStateLoading => state.state === "loading"

export const isSuccess = <T>(
  state: ApiMethodState<T>
): state is ApiMethodStateSuccess<T> =>
  state.state === "success" || state.state === "reloading"

export const isError = <T>(
  state: ApiMethodState<T>
): state is ApiMethodStateError => state.state === "error"

export const mapInitial = <T, S>(
  state: ApiMethodState<T>,
  fn: () => S | null
) => (isInitial(state) ? fn() : null)

export const mapLoading = <T, S>(
  state: ApiMethodState<T>,
  fn: () => S | null
) => (isLoading(state) ? fn() : null)

export const mapSuccess = <T, S>(
  state: ApiMethodState<T>,
  fn: (data: T) => S | null
) => (isSuccess(state) ? fn(state.data) : null)

export const mapError = <T, S>(
  state: ApiMethodState<T>,
  fn: (errors: ApiError[]) => S | null
) => (isError(state) ? fn(state.errors) : null)
