import * as E from "fp-ts/Either"
import { pipe } from "fp-ts/lib/function"
import { fetchCurrentUser } from "../api/api"

export type User = {
  oid: string
  username: string
  name: string
  serviceTicket: string
  kansalainen: boolean
  huollettava: boolean
}

export type CurrentUser = "unauthorized" | "forbidden" | User

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
