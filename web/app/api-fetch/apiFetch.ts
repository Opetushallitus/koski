import * as E from 'fp-ts/lib/Either'
import { pipe } from 'fp-ts/lib/function'
import Cookies from 'js-cookie'
import { parseErrors } from './apiErrors'

export type ApiSuccess<T> = {
  status: number
  data: T
}

export type ApiErrorKey =
  | 'invalid.json'
  | 'unauthorized.loginFail'
  | 'badRequest.validation.jsonSchema'

export type ApiError = {
  key?: ApiErrorKey
  messageKey: string
  messageData?: Record<string, string | number>
}

export type ApiFailure = {
  errors: ApiError[]
  status?: number
  data?: any
}

export type ApiResponse<T> = E.Either<ApiFailure, ApiSuccess<T>>

export type JsonRequestInit = Omit<RequestInit, 'body'> & { body: any }

const apiFetch = async <T>(
  input: RequestInfo,
  init?: RequestInit
): Promise<ApiResponse<T>> => {
  try {
    const response = await fetch(input, init)
    try {
      const data = response.status !== 204 ? await response.json() : null
      if (response.status < 400) {
        return E.right({
          status: response.status,
          data
        })
      }
      return E.left({
        errors: apiErrorMessages(response.status, data),
        status: response.status
      })
    } catch (err) {
      return E.left({
        errors: [
          {
            key: 'invalid.json',
            messageKey: 'apivirhe__virheellinen_vastaus'
          }
        ],
        status: response.status
      })
    }
  } catch (err) {
    return E.left({
      errors: parseErrors(err)
    })
  }
}

export const enrichJsonRequest = (
  method: string,
  accept: string,
  init?: JsonRequestInit
): JsonRequestInit => ({
  credentials: 'include',
  method,
  ...init,
  headers: {
    Accept: accept,
    'Content-Type': 'application/json',
    CSRF: Cookies.get('CSRF')!,
    'Caller-id': '1.2.246.562.10.00000000001.valpas.frontend',
    ...init?.headers
  },
  body: init?.body && JSON.stringify(init.body)
})

export const apiGet = async <T>(
  input: RequestInfo,
  init?: JsonRequestInit
): Promise<ApiResponse<T>> =>
  apiFetch<T>(input, enrichJsonRequest('GET', 'application/json', init))

export const apiPost = async <T>(
  input: RequestInfo,
  init?: JsonRequestInit
): Promise<ApiResponse<T>> =>
  apiFetch<T>(input, enrichJsonRequest('POST', 'application/json', init))

export const apiPut = async <T>(
  input: RequestInfo,
  init?: JsonRequestInit
): Promise<ApiResponse<T>> =>
  apiFetch<T>(input, enrichJsonRequest('PUT', 'application/json', init))

export const apiDelete = async <T>(
  input: RequestInfo,
  init?: JsonRequestInit
): Promise<ApiResponse<T>> =>
  apiFetch<T>(input, enrichJsonRequest('DELETE', 'application/json', init))

export const prependUrl = (
  baseUrl: string,
  request: RequestInfo
): RequestInfo =>
  typeof request === 'string'
    ? baseUrl + '/' + request
    : {
        ...request,
        url: baseUrl + '/' + request.url
      }

export const mockApi =
  <T, P extends any[]>(getResult: (...params: P) => E.Either<ApiError, T>) =>
  async (...params: P): Promise<ApiResponse<T>> => {
    await new Promise((resolve) =>
      setTimeout(resolve, 300 + Math.random() * 200)
    )
    return pipe(
      getResult(...params),
      E.map((data) => ({ status: 200, data })),
      E.mapLeft((error) => ({ errors: [error] }))
    )
  }

const apiErrorMessages = (status: number, error: unknown): ApiError[] => {
  const errorMessages = parseErrors(error)
  if (errorMessages.length > 0) {
    return errorMessages
  }

  if (status >= 400 && status < 500) {
    return [
      {
        messageKey: 'apivirhe__virheellinen_pyyntö',
        messageData: { virhe: status }
      }
    ]
  }

  if (status === 504) {
    return [{ messageKey: 'apivirhe__aikakatkaisu' }]
  }

  if (status >= 500 && status < 600) {
    return [
      {
        messageKey: 'apivirhe__palvelinongelma',
        messageData: { virhe: status }
      }
    ]
  }

  return []
}
