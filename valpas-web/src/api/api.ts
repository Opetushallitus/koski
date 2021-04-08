import {
  OppijaHakutilanteillaLaajatTiedot,
  OppijaHakutilanteillaSuppeatTiedot,
} from "../state/oppijat"
import { Oid, OrganisaatioJaKayttooikeusrooli, User } from "../state/types"
import { tapLeftP } from "../utils/either"
import { ApiFailure, apiGet, apiPost } from "./apiFetch"
import { createCache } from "./cache"

export const healthCheck = async () =>
  apiGet<string>("api/healthcheck/internal")

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
 * Hae lista organisaatioista käyttöoikeuksien kanssa
 */
export const fetchYlatasonOrganisaatiotJaKayttooikeusroolit = async () =>
  handleExpiredSession(
    apiGet<OrganisaatioJaKayttooikeusrooli[]>(
      "valpas/api/organisaatiot-ja-kayttooikeusroolit"
    )
  )

export const fetchYlatasonOrganisaatiotJaKayttooikeusroolitCache = createCache(
  fetchYlatasonOrganisaatiotJaKayttooikeusroolit
)

/**
 * Hae suppeat tiedot oppijoista
 */
export const fetchOppijat = (organisaatioOid: Oid) =>
  handleExpiredSession(
    apiGet<OppijaHakutilanteillaSuppeatTiedot[]>(
      `valpas/api/oppijat/${organisaatioOid}`
    )
  )

export const fetchOppijatCache = createCache(fetchOppijat)

/**
 * Hae yksittäisen oppijan laajat tiedot
 */
export const fetchOppija = (oppijaOid: Oid) =>
  handleExpiredSession(
    apiGet<OppijaHakutilanteillaLaajatTiedot>(`valpas/api/oppija/${oppijaOid}`)
  )

export const fetchOppijaCache = createCache(fetchOppija)

// Virhetilanteiden hallinta

const handleExpiredSession = tapLeftP((failure: ApiFailure) => {
  if (failure.status === 401) {
    // 401 Unauthorized -> käyttäjä ei ole (enää) kirjautunut -> lähetä kirjautumiseen lataamalla sivu uudelleen.
    // Kirjautuminen ei laukea siitä, että käyttäjä yritti nähdä tietoa, johon hänellä ei ole oikeutta,
    // koska Valpas-APIt palauttavat siinä tapauksessa 403 Forbidden.
    location.reload()
  }
})
