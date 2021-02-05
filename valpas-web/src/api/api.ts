import * as A from "fp-ts/Array"
import * as E from "fp-ts/Either"
import { pipe } from "fp-ts/lib/function"
import { mockOppijat } from "../state/mock"
import { Oppija } from "../state/oppijat"
import { Organisaatio, User } from "../state/types"
import { apiGet, apiPost, mockApi } from "./apiFetch"
import { createCache } from "./cache"

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

export const fetchOrganisaatiotCache = createCache(fetchOrganisaatiot)

/**
 * Get oppijat
 */
export const fetchOppijat = mockApi<Oppija[], []>(() => E.right(mockOppijat))
export const fetchOppijatCache = createCache(fetchOppijat)

export const fetchOppija = mockApi<Oppija, [string]>((oid) =>
  pipe(
    A.findFirst((oppija: Oppija) => oppija.oid === oid)(mockOppijat),
    E.fromOption(() => ({ message: "Not found" }))
  )
)
export const fetchOppijaCache = createCache(fetchOppija)
