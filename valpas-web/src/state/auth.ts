import * as E from "fp-ts/Either"
import { pipe } from "fp-ts/lib/function"
import { fetchCurrentUser } from "../api/api"
import { absoluteUrl, buildUrl } from "../utils/url"

export type User = {
  oid: string
  username: string
  name: string
  serviceTicket: string
  kansalainen: boolean
  huollettava: boolean
}

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
  const opintopolkuVirkailijaUrl = process.env.OPINTOPOLKU_VIRKAILIJA_URL
  return opintopolkuVirkailijaUrl
    ? {
        type: "external",
        redirectToVirkailijaLogin() {
          location.href = buildUrl(`${opintopolkuVirkailijaUrl}/cas/login`, {
            service: absoluteUrl("/cas/virkailija"),
            redirect: location.href,
          })
        },
      }
    : {
        type: "local",
      }
}
