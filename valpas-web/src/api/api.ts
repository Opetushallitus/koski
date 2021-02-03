import * as A from "fp-ts/Array"
import * as E from "fp-ts/Either"
import { pipe } from "fp-ts/lib/function"
import { mockOppijat, Oppija } from "../state/oppijat"
import { Organisaatio, User } from "../state/types"
import { apiGet, apiPost, mockApi } from "./apiFetch"

/**
 * Login
 */
export const fetchLogin = async (username: string, password: string) =>
  apiPost<User>("valpas/login", {
    body: {
      username,
      password,
    },
  })

/**
 * Hae kirjautuneen käyttäjän tiedot
 */
export const fetchCurrentUser = async () => apiGet<User>("valpas/api/user")

/**
 * Hae lista organisaatioista, joihin käyttäjällä on käyttöoikeus
 */
export const fetchOrganisaatiot = async () =>
  apiGet<Organisaatio[]>("valpas/api/organisaatiot")

/**
 * Get oppijat
 */
export const fetchOppijat = mockApi<Oppija[], []>(() => E.right(mockOppijat))
export const fetchOppija = mockApi<Oppija, [string]>((oid) =>
  pipe(
    A.findFirst((oppija: Oppija) => oppija.oid === oid)(mockOppijat),
    E.fromOption(() => ({ message: "Not found" }))
  )
)
