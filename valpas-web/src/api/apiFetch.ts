// TODO: Hieman parempi poikkeustenhallinta, ehkä fp-ts/Either paluuarvoksi

export type ApiResponse<T> =
  | {
      type: "resolved"
      status: number
      data: T
    }
  | {
      type: "rejected"
      message: number
    }

export const apiFetch = async <T>(
  input: RequestInfo,
  init?: RequestInit
): Promise<ApiResponse<T>> => {
  try {
    const response = await fetch(
      prependUrl(process.env.BACKEND_URL || "", input),
      init
    )
    return {
      type: "resolved",
      status: response.status,
      data: (await response.json()) as T,
    }
  } catch (err) {
    return {
      type: "rejected",
      message: err.message,
    }
  }
}

const prependUrl = (baseUrl: string, request: RequestInfo): RequestInfo =>
  typeof request === "string"
    ? baseUrl + "/" + request
    : {
        ...request,
        url: baseUrl + "/" + request.url,
      }
