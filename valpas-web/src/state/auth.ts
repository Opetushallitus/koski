import * as E from "fp-ts/Either"
import { pipe } from "fp-ts/lib/function"
import Cookies from "js-cookie"
import { fetchCurrentUser } from "../api/api"
import { absoluteKoskiUrl, buildUrl } from "../utils/url"
import { User } from "./types"

const RETURN_URL_KEY = "koskiReturnUrl"

export type CurrentUser = "unauthorized" | "forbidden" | User

export type ExternalLogin = {
  type: "external"
  redirectToVirkailijaLogin(): void
}

export type LocalLogin = {
  type: "local"
}

export type Login = ExternalLogin | LocalLogin

export const getCurrentUser = async (): Promise<CurrentUser> =>
  pipe(
    await fetchCurrentUser(),
    E.map((response) => response.data as CurrentUser),
    E.getOrElse(
      (fail) =>
        (fail.status === 403 ? "forbidden" : "unauthorized") as CurrentUser
    )
  )

export const isLoggedIn = (user: CurrentUser) => user !== "unauthorized"
export const hasValpasAccess = (user: CurrentUser): user is User =>
  isLoggedIn(user) && user !== "forbidden"

export const getLogin = (): Login => {
  const opintopolkuVirkailijaUrl =
    process.env.OPINTOPOLKU_VIRKAILIJA_URL || window.opintopolkuVirkailijaUrl
  return opintopolkuVirkailijaUrl && opintopolkuVirkailijaUrl !== "mock"
    ? {
        type: "external",
        redirectToVirkailijaLogin() {
          location.href = buildUrl(`${opintopolkuVirkailijaUrl}/cas/login`, {
            service: absoluteKoskiUrl("/cas/virkailija"),
          })
        },
      }
    : {
        type: "local",
      }
}

export const storeLoginReturnUrl = () => {
  if (!Cookies.get(RETURN_URL_KEY)) {
    Cookies.set(RETURN_URL_KEY, location.href)
  }
}

export const redirectToLoginReturnUrl = (): boolean => {
  const url = Cookies.get(RETURN_URL_KEY)
  if (url) {
    Cookies.remove(RETURN_URL_KEY)
    location.href = url
    return true
  }
  return false
}
