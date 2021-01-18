import * as O from "fp-ts/Option"
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

export const getCurrentUser = async () =>
  pipe(
    await fetchCurrentUser(),
    O.fromEither,
    O.map((response) => response.data),
    O.toNullable
  )
