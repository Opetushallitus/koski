// TODO: Hieman parempi poikkeustenhallinta, ehkä fp-ts/Either paluuarvoksi

import { Either, right, left } from "fp-ts/lib/Either"

export type ApiSuccess<T> = {
  status: number
  data: T
}

export type ApiError = {
  message: string
  status?: number
  data?: any
}

export type ApiResponse<T> = Either<ApiError, ApiSuccess<T>>

export const apiFetch = async <T>(
  input: RequestInfo,
  init?: RequestInit
): Promise<ApiResponse<T>> => {
  try {
    const response = await fetch(
      prependUrl(process.env.BACKEND_URL || "", input),
      init
    )
    try {
      const data = (await response.json()) as T
      return right({
        status: response.status,
        data,
      })
    } catch (err) {
      return left({
        message: "Response is not valid JSON",
        status: response.status,
        data: await response.text(),
      })
    }
  } catch (err) {
    return left({
      message: err.message,
    })
  }
}

const prependUrl = (baseUrl: string, request: RequestInfo): RequestInfo =>
  typeof request === "string"
    ? baseUrl + "/" + request
    : {
        ...request,
        url: baseUrl + "/" + request.url,
      }
