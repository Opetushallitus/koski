import download from "downloadjs"
import * as E from "fp-ts/Either"
import { pipe } from "fp-ts/lib/function"
import * as O from "fp-ts/Option"
import { t } from "../i18n/i18n"
import { parseJson } from "../utils/objects"
import { parseErrors } from "./apiErrors"
import {
  ApiError,
  ApiResponse,
  enrichJsonRequest,
  JsonRequestInit,
  prependUrl,
} from "./apiFetch"

export const apiPostDownload = async (
  defaultFilename: string,
  input: RequestInfo,
  init?: JsonRequestInit,
): Promise<ApiResponse<Blob>> => {
  try {
    const response = await fetch(
      prependUrl("/koski", input),
      enrichJsonRequest("POST", "*/*", init),
    )
    const data = await response.blob()

    if (response.status < 400) {
      download(
        data,
        parseFilename(response.headers.get("content-disposition")) ||
          defaultFilename,
        response.headers.get("content-type") || "application/octet-stream",
      )
      return E.right({
        status: response.status,
        data,
      })
    } else {
      return E.left({
        status: response.status,
        errors: await parseDownloadError(data),
      })
    }
  } catch (e: any) {
    return E.left({
      errors: parseErrors(e),
    })
  }
}

const parseDownloadError = async (blob: Blob): Promise<ApiError[]> =>
  pipe(
    await blob.text(),
    parseJson,
    O.map(parseErrors),
    O.getOrElse(() => [{ message: t("tiedoston_lataus_epäonnistui") }]),
  )

const parseFilename = (header: string | null): string | null =>
  (header || "").match(/filename="(.*?)"/)?.[1] || null
