import { AppConfiguration } from "../state/apitypes/appConfiguration"
import { HenkilöhakuResult } from "../state/apitypes/henkilohaku"
import { KansalaisnäkymänTiedot } from "../state/apitypes/kansalainen"
import { KuntailmoitusLaajatTiedotOppijaOidilla } from "../state/apitypes/kuntailmoitus"
import { KuntailmoitusPohjatiedot } from "../state/apitypes/kuntailmoituspohjatiedot"
import { OpiskeluoikeusSuppeatTiedot } from "../state/apitypes/opiskeluoikeus"
import {
  OppijaHakutilanteillaLaajatTiedot,
  OppijaHakutilanteillaSuppeatTiedot,
  OppijaKuntailmoituksillaSuppeatTiedot,
} from "../state/apitypes/oppija"
import {
  OppivelvollisuudenKeskeytyksenMuutos,
  UusiOppivelvollisuudenKeskeytys,
} from "../state/apitypes/oppivelvollisuudenkeskeytys"
import {
  OppivelvollisuudestaVapautuksenMitätöinti,
  OppivelvollisuudestaVapautuksenPohjatiedot,
  UusiOppivelvollisuudestaVapautus,
} from "../state/apitypes/oppivelvollisuudestavapautus"
import {
  HetuhakuInput,
  KuntarouhinnanTulos,
  KuntarouhintaInput,
} from "../state/apitypes/rouhinta"
import {
  Hetu,
  Oid,
  OrganisaatioJaKayttooikeusrooli,
  User,
} from "../state/common"
import { queryPath } from "../state/paths"
import { tapLeftP } from "../utils/either"
import { apiPostDownload } from "./apiDownload"
import { apiDelete, ApiFailure, apiGet, apiPost, apiPut } from "./apiFetch"
import { createLocalThenApiCache, createPreferLocalCache } from "./cache"

const SPREADSHEET_CONTENT_TYPE =
  "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

export const healthCheck = async () =>
  apiGet<string>("api/healthcheck/internal")

/**
 * Window properties
 */

export const fetchAppConfiguration = () =>
  apiGet<AppConfiguration>("valpas/localization/window-properties")

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
export const fetchCurrentVirkailijaUser = async () =>
  apiGet<User>("valpas/api/user")
export const fetchCurrentKansalainenUser = async () =>
  apiGet<User>("valpas/api/kansalainen/user")

/**
 * Hae lista organisaatioista käyttöoikeuksien kanssa
 */
export const fetchYlatasonOrganisaatiotJaKayttooikeusroolit = async () =>
  handleExpiredSession(
    apiGet<OrganisaatioJaKayttooikeusrooli[]>(
      "valpas/api/organisaatiot-ja-kayttooikeusroolit",
    ),
  )

export const fetchYlatasonOrganisaatiotJaKayttooikeusroolitCache =
  createLocalThenApiCache(fetchYlatasonOrganisaatiotJaKayttooikeusroolit)

/**
 * Hae suppeat tiedot oppijoista
 */
export const fetchOppijat = (organisaatioOid: Oid) =>
  handleExpiredSession(
    apiGet<OppijaHakutilanteillaSuppeatTiedot[]>(
      `valpas/api/oppijat/${organisaatioOid}`,
    ),
  )

export const fetchOppijatCache = createPreferLocalCache(fetchOppijat)

/**
 * Hae suppeat tiedot oppijoista hakutiedoilla annetun listan oppijoista
 */
export const fetchOppijatHakutiedoilla = (
  organisaatioOid: Oid,
  oppijaOids: Oid[],
) =>
  handleExpiredSession(
    apiPost<OppijaHakutilanteillaSuppeatTiedot[]>(
      `valpas/api/oppijat/${organisaatioOid}/hakutiedot`,
      {
        body: {
          oppijaOids,
        },
      },
    ),
  )

export const fetchOppijatHakutiedoillaCache = createPreferLocalCache(
  fetchOppijatHakutiedoilla,
)

/**
 * Hae suppeat tiedot nivelvaiheen oppijoista
 */
export const fetchNivelvaiheenOppijat = (organisaatioOid: Oid) =>
  handleExpiredSession(
    apiGet<OppijaHakutilanteillaSuppeatTiedot[]>(
      `valpas/api/oppijat-nivelvaihe/${organisaatioOid}`,
    ),
  )

export const fetchNivelvaiheenOppijatCache = createPreferLocalCache(
  fetchNivelvaiheenOppijat,
)

/**
 * Hae suppeat tiedot nivelvaiheen oppijoista hakutiedoilla annetun listan oppijoista
 */
export const fetchNivelvaiheenOppijatHakutiedoilla = (
  organisaatioOid: Oid,
  oppijaOids: Oid[],
) =>
  handleExpiredSession(
    apiPost<OppijaHakutilanteillaSuppeatTiedot[]>(
      `valpas/api/oppijat-nivelvaihe/${organisaatioOid}/hakutiedot`,
      {
        body: {
          oppijaOids,
        },
      },
    ),
  )

export const fetchNivelvaiheenOppijatHakutiedoillaCache =
  createPreferLocalCache(fetchNivelvaiheenOppijatHakutiedoilla)

/**
 * Hae hakeutumisvalvonnan kunnalle tekemät ilmoitukset
 */
export const fetchKunnalleTehdytIlmoitukset = (organisaatioOid: Oid) =>
  handleExpiredSession(
    apiGet<OppijaHakutilanteillaSuppeatTiedot[]>(
      `valpas/api/oppijat/${organisaatioOid}/ilmoitukset`,
    ),
  )

export const fetchKunnalleTehdytIlmoituksetCache = createLocalThenApiCache(
  fetchKunnalleTehdytIlmoitukset,
)

/**
 * Hae suppeat tiedot oppijoista suorittamisen valvontanäkymään
 */
export const fetchOppijatSuorittaminen = (organisaatioOid: Oid) =>
  handleExpiredSession(
    apiGet<OppijaHakutilanteillaSuppeatTiedot[]>(
      `valpas/api/oppijat-suorittaminen/${organisaatioOid}`,
    ),
  )

export const fetchOppijatSuorittaminenCache = createPreferLocalCache(
  fetchOppijatSuorittaminen,
)

/**
 * Hae yksittäisen oppijan laajat tiedot
 */
export const fetchOppija = (oppijaOid: Oid) =>
  handleExpiredSession(
    apiGet<OppijaHakutilanteillaLaajatTiedot>(`valpas/api/oppija/${oppijaOid}`),
  )

export const fetchOppijaCache = createLocalThenApiCache(fetchOppija)

/**
 * Etsi henkilöä hetulla/oidilla maksuttomuuskäyttäjänä
 */
export const fetchHenkilöhakuMaksuttomuus = (query: Oid | Hetu) =>
  handleExpiredSession(
    apiGet<HenkilöhakuResult>(`valpas/api/henkilohaku/maksuttomuus/${query}`),
  )

export const fetchHenkilöhakuMaksuttomuusCache = createLocalThenApiCache(
  fetchHenkilöhakuMaksuttomuus,
)

/**
 * Etsi henkilöä hetulla/oidilla suorittamisen valvojana
 */
export const fetchHenkilöhakuSuorittaminen = (query: Oid | Hetu) =>
  handleExpiredSession(
    apiGet<HenkilöhakuResult>(`valpas/api/henkilohaku/suorittaminen/${query}`),
  )

export const fetchHenkilöhakuSuorittaminenCache = createLocalThenApiCache(
  fetchHenkilöhakuSuorittaminen,
)

/**
 * Etsi henkilöä hetulla/oidilla kunnan käyttäjänä
 */
export const fetchHenkilöhakuKunta = (query: Oid | Hetu) =>
  handleExpiredSession(
    apiGet<HenkilöhakuResult>(`valpas/api/henkilohaku/kunta/${query}`),
  )

export const fetchHenkilöhakuKuntaCache = createLocalThenApiCache(
  fetchHenkilöhakuKunta,
)

/**
 * Kunnan rouhinta hetulistalla
 */
export const downloadRouhintaHetuilla = (query: HetuhakuInput) =>
  handleExpiredSession(
    apiPostDownload("hetuhaku.xlsx", "valpas/api/rouhinta/hetut", {
      body: query,
      headers: {
        accept: SPREADSHEET_CONTENT_TYPE,
      },
    }),
  )

/**
 * Kunnan rouhintalista (json)
 */
export const fetchKuntarouhinta = (query: KuntarouhintaInput) =>
  handleExpiredSession(
    apiPost<KuntarouhinnanTulos>("valpas/api/rouhinta/kunta", {
      body: query,
    }),
  )

export const fetchKuntarouhintaCache =
  createPreferLocalCache(fetchKuntarouhinta)

/**
 * Kunnan rouhintalista (spreadsheet)
 */
export const downloadKuntarouhinta = (query: KuntarouhintaInput) =>
  handleExpiredSession(
    apiPostDownload("kuntahaku.xlsx", "valpas/api/rouhinta/kunta", {
      body: query,
      headers: {
        accept: SPREADSHEET_CONTENT_TYPE,
      },
    }),
  )

/**
 * Kuntailmoituksen pohjatietojen haku
 */
export const fetchKuntailmoituksenPohjatiedot = (
  oppijaOids: Oid[],
  tekijäOrganisaatioOid?: Oid,
) =>
  handleExpiredSession(
    apiPost<KuntailmoitusPohjatiedot>("valpas/api/kuntailmoitus/pohjatiedot", {
      body: {
        tekijäOrganisaatio: tekijäOrganisaatioOid
          ? {
              oid: tekijäOrganisaatioOid,
            }
          : undefined,
        oppijaOidit: oppijaOids,
      },
    }),
  )

/**
 * Kuntailmoituksen tallennus
 */
export const createKuntailmoitus = (
  kuntailmoitus: KuntailmoitusLaajatTiedotOppijaOidilla,
) =>
  handleExpiredSession(
    apiPost<void>("valpas/api/kuntailmoitus", {
      body: kuntailmoitus,
    }),
  )

/**
 * Kuntailmoitusten hakeminen
 */
export const fetchKuntailmoitukset = (kuntaOid: Oid) =>
  handleExpiredSession(
    apiGet<OppijaKuntailmoituksillaSuppeatTiedot[]>(
      `valpas/api/kuntailmoitus/oppijat/${kuntaOid}`,
    ),
  )

export const fetchKuntailmoituksetCache = createPreferLocalCache(
  fetchKuntailmoitukset,
)

/**
 * Tallenna muu haku -valitsimen tila
 * @param oppijaOid
 * @param opiskeluoikeusOid
 * @param oppilaitosOid
 * @param value
 * @returns
 */
export const setMuuHaku = async (
  oppijaOid: Oid,
  opiskeluoikeus: OpiskeluoikeusSuppeatTiedot,
  value: boolean,
) =>
  handleExpiredSession(
    apiPut(
      queryPath(`valpas/api/oppija/${oppijaOid}/set-muu-haku`, {
        opiskeluoikeusOid: opiskeluoikeus.oid,
        oppilaitosOid: opiskeluoikeus.oppilaitos.oid,
        value,
      }),
    ),
  )

/**
 * Oppivelvollisuuden keskeytyksen lisäys
 */
export const createOppivelvollisuudenKeskeytys = (
  keskeytys: UusiOppivelvollisuudenKeskeytys,
) =>
  handleExpiredSession(
    apiPost<void>("valpas/api/oppija/ovkeskeytys", { body: keskeytys }),
  )

/**
 * Oppivelvollisuuden keskeytyksen päivitys
 */
export const updateOppivelvollisuudenKeskeytys = (
  keskeytys: OppivelvollisuudenKeskeytyksenMuutos,
) =>
  handleExpiredSession(
    apiPut<void>("valpas/api/oppija/ovkeskeytys", { body: keskeytys }),
  )

/**
 * Oppivelvollisuudesta vapauttamisen pohjatietojen haku
 */
export const fetchOvVapautuksenPohjatiedot = () =>
  handleExpiredSession(
    apiGet<OppivelvollisuudestaVapautuksenPohjatiedot>(
      "valpas/api/vapautus/pohjatiedot",
    ),
  )

export const createOvVapautus = (vapautus: UusiOppivelvollisuudestaVapautus) =>
  handleExpiredSession(apiPost<null>("valpas/api/vapautus", { body: vapautus }))

export const deleteOvVapautus = (
  vapautus: OppivelvollisuudestaVapautuksenMitätöinti,
) =>
  handleExpiredSession(
    apiDelete<null>("valpas/api/vapautus", { body: vapautus }),
  )

/**
 * Oppivelvollisuuden keskeytyksen poisto
 */
export const deleteOppivelvollisuudenKeskeytys = (id: string) =>
  handleExpiredSession(apiDelete<void>(`valpas/api/oppija/ovkeskeytys/${id}`))

/**
 * Kansalaisen omien ja huollettavien tietojen hakeminen
 */
export const fetchOmatJaHuollettavienTiedot = () =>
  handleExpiredSession(
    apiGet<KansalaisnäkymänTiedot>("valpas/api/kansalainen/tiedot"),
  )

// Virhetilanteiden hallinta

const handleExpiredSession = tapLeftP((failure: ApiFailure) => {
  if (failure.status === 401) {
    // 401 Unauthorized -> käyttäjä ei ole (enää) kirjautunut -> lähetä kirjautumiseen lataamalla sivu uudelleen.
    // Kirjautuminen ei laukea siitä, että käyttäjä yritti nähdä tietoa, johon hänellä ei ole oikeutta,
    // koska Valpas-APIt palauttavat siinä tapauksessa 403 Forbidden.
    location.reload()
  }
})
