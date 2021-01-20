import * as E from "fp-ts/lib/Either"

export type ApiSuccess<T> = {
  status: number
  data: T
}

export type ApiErrorKey = "invalid.json" | "unauthorized.loginFail"

export type ApiError = {
  key?: ApiErrorKey
  message: string
}

export type ApiFailure = {
  errors: ApiError[]
  status?: number
  data?: any
}

export type ApiResponse<T> = E.Either<ApiFailure, ApiSuccess<T>>

const apiFetch = async <T>(
  input: RequestInfo,
  init?: RequestInit
): Promise<ApiResponse<T>> => {
  try {
    const response = await fetch(
      prependUrl(process.env.BACKEND_PROXY || "", input),
      init
    )
    try {
      const data = await response.json()
      return response.status < 400
        ? E.right({
            status: response.status,
            data,
          })
        : E.left({
            errors: data,
          })
    } catch (err) {
      return E.left({
        errors: [
          {
            key: "invalid.json",
            message: "Response is not valid JSON",
          },
        ],
        status: response.status,
      })
    }
  } catch (err) {
    return E.left({
      errors: [{ message: err.message }],
    })
  }
}

type JsonRequestInit = Omit<RequestInit, "body"> & { body: any }

const enrichRequest = (
  method: string,
  init?: JsonRequestInit
): JsonRequestInit => ({
  credentials: "include",
  method,
  ...init,
  headers: {
    "Content-Type": "application/json",
    ...init?.headers,
  },
  body: init?.body && JSON.stringify(init.body),
})

export const apiGet = async <T>(
  input: RequestInfo,
  init?: JsonRequestInit
): Promise<ApiResponse<T>> => apiFetch<T>(input, enrichRequest("GET", init))

export const apiPost = async <T>(
  input: RequestInfo,
  init?: JsonRequestInit
): Promise<ApiResponse<T>> => apiFetch<T>(input, enrichRequest("POST", init))

const prependUrl = (baseUrl: string, request: RequestInfo): RequestInfo =>
  typeof request === "string"
    ? baseUrl + "/" + request
    : {
        ...request,
        url: baseUrl + "/" + request.url,
      }
