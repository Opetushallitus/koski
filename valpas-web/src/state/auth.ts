import * as E from "fp-ts/Either"
import { pipe } from "fp-ts/lib/function"
import Cookies from "js-cookie"
import {
  fetchCurrentKansalainenUser,
  fetchCurrentVirkailijaUser,
} from "../api/api"
import { ApiResponse } from "../api/apiFetch"
import { koskiBackendHost } from "../utils/environment"
import { absoluteKoskiUrl, buildUrl } from "../utils/url"
import { User } from "./common"

const RETURN_URL_KEY = "koskiReturnUrl"

export type CurrentUser = "unauthorized" | "forbidden" | User

export type ExternalLogin = {
  type: "external"
  redirectToExternalLogin(): void
}

export type LocalLogin = {
  type: "local"
}

export type Login = ExternalLogin | LocalLogin

const getCurrentUser =
  (fetchFn: () => Promise<ApiResponse<User>>) =>
  async (): Promise<CurrentUser> =>
    pipe(
      await fetchFn(),
      E.map((response) => response.data as CurrentUser),
      E.getOrElse(
        (fail) =>
          (fail.status === 403 ? "forbidden" : "unauthorized") as CurrentUser
      )
    )

export const getCurrentVirkailijaUser = getCurrentUser(
  fetchCurrentVirkailijaUser
)
export const getCurrentKansalainenUser = getCurrentUser(
  fetchCurrentKansalainenUser
)

export const isLoggedIn = (user: CurrentUser) => user !== "unauthorized"
export const hasValpasAccess = (user: CurrentUser): user is User =>
  isLoggedIn(user) && user !== "forbidden"

export const getVirkailijaLogin = (): Login => {
  const url =
    process.env.OPINTOPOLKU_VIRKAILIJA_URL || window.opintopolkuVirkailijaUrl
  return isLoginUrl(url)
    ? casLogin(`${url}/cas/login`, "/cas/virkailija")
    : mockValpasVirkailijaLogin()
}

export const getOppijaLogin = (): Login => {
  const url = process.env.OPINTOPOLKU_OPPIJA_URL || window.opintopolkuOppijaUrl
  return isLoginUrl(url)
    ? casLogin(`${url}/cas-oppija/login`, "/cas/valpas/oppija")
    : mockKoskiOppijaLogin()
}

const isLoginUrl = (url: string | undefined): boolean =>
  Boolean(url && url !== "mock")

const casLogin = (url: string, casServiceUrl: string): Login =>
  url && url !== "mock"
    ? {
        type: "external",
        redirectToExternalLogin() {
          location.href = buildUrl(url, {
            service: absoluteKoskiUrl(casServiceUrl),
          })
        },
      }
    : {
        type: "local",
      }

const mockValpasVirkailijaLogin = (): Login => ({ type: "local" })

const mockKoskiOppijaLogin = (): Login => ({
  type: "external",
  redirectToExternalLogin() {
    location.href = buildUrl(`${koskiBackendHost}/koski/login/oppija/local`, {
      redirect: location.href,
    })
  },
})

export const storeLoginReturnUrl = (url: string) => {
  Cookies.set(RETURN_URL_KEY, url)
}

export const redirectToLoginReturnUrl = (): boolean => {
  const url = Cookies.get(RETURN_URL_KEY)
  if (url) {
    Cookies.remove(RETURN_URL_KEY)
    location.replace(url)
    return true
  }
  return false
}
