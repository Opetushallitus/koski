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
      saveFile(
        data,
        parseFilename(response.headers.get("content-disposition")) ||
          defaultFilename,
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
  } catch (e) {
    return E.left({
      errors: parseErrors(e),
    })
  }
}

const saveFile = (blob: Blob, filename: string) => {
  const url = URL.createObjectURL(blob)
  const link = document.createElement("a")
  link.href = url
  link.download = filename
  link.click()
  // Vapauta muisti viiveellä, jotta lataus toimii myös joissain vanhemmissa
  // selaimissa
  setTimeout(() => URL.revokeObjectURL(url), 5000)
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
