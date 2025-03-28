import { apiDelete, ApiFailure, apiGet, apiPost, apiPut } from '../api-fetch'
import { lang } from '../i18n/i18n'
import { OpiskeluoikeusHistoryPatch } from '../types/fi/oph/koski/history/OpiskeluoikeusHistoryPatch'
import { HenkilönOpiskeluoikeusVersiot } from '../types/fi/oph/koski/oppija/HenkilonOpiskeluoikeusVersiot'
import { OrganisaatioHierarkia } from '../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { KeyValue } from '../types/fi/oph/koski/preferences/KeyValue'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { OidHenkilö } from '../types/fi/oph/koski/schema/OidHenkilo'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'
import { Oppija } from '../types/fi/oph/koski/schema/Oppija'
import { PäätasonSuoritus } from '../types/fi/oph/koski/schema/PaatasonSuoritus'
import { StorablePreference } from '../types/fi/oph/koski/schema/StorablePreference'
import { Suoritus } from '../types/fi/oph/koski/schema/Suoritus'
import { Osaamismerkkikuva } from '../types/fi/oph/koski/servlet/Osaamismerkkikuva'
import { AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä } from '../types/fi/oph/koski/suoritusjako/AktiivisetJaPaattyneetOpinnotOppijaJakolinkilla'
import { SuoritetutTutkinnotOppijaJakolinkillä } from '../types/fi/oph/koski/suoritusjako/SuoritetutTutkinnotOppijaJakolinkilla'
import { TutkintoPeruste } from '../types/fi/oph/koski/tutkinto/TutkintoPeruste'
import { Constraint } from '../types/fi/oph/koski/typemodel/Constraint'
import { GroupedKoodistot } from '../types/fi/oph/koski/typemodel/GroupedKoodistot'
import { OpiskeluoikeusClass } from '../types/fi/oph/koski/typemodel/OpiskeluoikeusClass'
import { YtrCertificateResponse } from '../types/fi/oph/koski/ytr/YtrCertificateResponse'
import { tapLeftP } from './fp/either'
import { TypedEnumValue } from './TypedEnumValue'
import { queryString } from './url'

const apiUrl = (path: string, query?: object): string =>
  `/koski/api/${path}${queryString({ class_refs: 'true', ...query })}`

export const fetchOppija = (oppijaOid: string) =>
  handleExpiredSession(apiGet<Oppija>(apiUrl(`oppija/${oppijaOid}/uiv2`)))

export const fetchOpiskeluoikeus = (
  opiskeluoikeusOid: string,
  version?: number
) =>
  handleExpiredSession(
    apiGet<Opiskeluoikeus>(
      version === undefined
        ? apiUrl(`opiskeluoikeus/${opiskeluoikeusOid}`)
        : apiUrl(`opiskeluoikeus/historia/${opiskeluoikeusOid}/${version}`)
    )
  )

export const fetchVersiohistoria = (opiskeluoikeusOid: string) =>
  handleExpiredSession(
    apiGet<OpiskeluoikeusHistoryPatch[]>(
      apiUrl(`opiskeluoikeus/historia/${opiskeluoikeusOid}`)
    )
  )

export const saveOpiskeluoikeus =
  (oppijaOid: string) => (opiskeluoikeus: Opiskeluoikeus) =>
    handleExpiredSession(
      apiPut<HenkilönOpiskeluoikeusVersiot>(
        apiUrl('oppija', { class_refs: false }),
        {
          body: Oppija({
            henkilö: OidHenkilö({ oid: oppijaOid }),
            opiskeluoikeudet: [opiskeluoikeus]
          })
        }
      )
    )

export const deletePäätasonSuoritus = (
  opiskeluoikeusOid: string,
  versionumero: number,
  suoritus: PäätasonSuoritus
) =>
  handleExpiredSession(
    apiPost<HenkilönOpiskeluoikeusVersiot>(
      apiUrl(
        `opiskeluoikeus/${opiskeluoikeusOid}/${versionumero}/delete-paatason-suoritus`
      ),
      {
        body: suoritus
      }
    )
  )

export const fetchKoodistot = (koodistoUris: string[]) =>
  handleExpiredSession(
    apiGet<GroupedKoodistot>(apiUrl(`types/koodisto/${koodistoUris.join(',')}`))
  )

export const fetchPeruste = (diaarinumero: string) =>
  handleExpiredSession(
    apiGet<
      Omit<
        Koodistokoodiviite<string, string>,
        '$class' | 'koodistoVersio' | 'lyhytNimi'
      >
    >(
      apiUrl(
        `tutkinnonperusteet/diaarinumerot/suorituksentyyppi/${diaarinumero}`
      )
    )
  )

export const fetchOppilaitoksenPerusteet = (
  oppilaitosOid: string,
  query?: string
) =>
  handleExpiredSession(
    apiGet<TutkintoPeruste[]>(
      apiUrl(`tutkinnonperusteet/oppilaitos/${oppilaitosOid}`, { query })
    )
  )

export interface Perustelinkki {
  url: string
}

export const fetchPerustelinkki = (diaarinumero: string) =>
  handleExpiredSession(
    apiGet<Perustelinkki>(
      apiUrl(
        `tutkinnonperusteet/peruste/${diaarinumero}/linkki?lang=${encodeURIComponent(
          lang
        )}`
      )
    )
  )

export const fetchConstraint = (
  schemaClass: string,
  shallow: boolean = false
) =>
  handleExpiredSession(
    apiGet<Constraint>(
      apiUrl(`types/constraints/${schemaClass}`, {
        shallow: shallow ? 'true' : undefined
      })
    )
  )

export type OrgTypesToShow =
  | 'vainOmatOrganisaatiot'
  | 'vainVarhaiskasvatusToimipisteet'

export const fetchOrganisaatioHierarkia = (orgTypesToShow?: OrgTypesToShow) =>
  handleExpiredSession(
    apiGet<OrganisaatioHierarkia[]>(
      apiUrl(`organisaatio/hierarkia`, { orgTypesToShow })
    )
  )

export const queryOrganisaatioHierarkia = (
  query: string,
  orgTypesToShow?: OrgTypesToShow,
  all?: boolean
) =>
  handleExpiredSession(
    apiGet<OrganisaatioHierarkia[]>(
      apiUrl(`organisaatio/hierarkia`, { query, orgTypesToShow, all })
    )
  )

export const fetchPreferences = <T extends StorablePreference>(
  organisaatioOid: string,
  type: string
) =>
  handleExpiredSession(
    apiGet<T[]>(apiUrl(`preferences/${organisaatioOid}/${type}`))
  )

export const storePreference = (
  organisaatioOid: string,
  type: string,
  key: string,
  value: StorablePreference
) =>
  handleExpiredSession(
    apiPut<void>(apiUrl(`preferences/${organisaatioOid}/${type}`), {
      body: KeyValue({ key, value })
    })
  )

export const removePreference = (
  organisaatioOid: string,
  type: string,
  key: string
) =>
  handleExpiredSession(
    apiDelete<void>(apiUrl(`preferences/${organisaatioOid}/${type}/${key}`))
  )

export const invalidateOpiskeluoikeus = (opiskeluoikeusOid: string) =>
  handleExpiredSession(
    apiDelete<void>(apiUrl(`opiskeluoikeus/${opiskeluoikeusOid}`))
  )

export const puraLähdejärjestelmäkytkentä = (opiskeluoikeusOid: string) =>
  handleExpiredSession(
    apiPost<void>(
      apiUrl(
        `opiskeluoikeus/${opiskeluoikeusOid}/pura-lahdejarjestelmakytkenta`
      )
    )
  )

export const fetchOmatTiedotOppija = () =>
  handleExpiredSession(apiGet<Oppija>(apiUrl('omattiedotV2/oppija')))

export const fetchSuoritusjako = (id: string) =>
  handleExpiredSession(
    apiPost<Oppija>(apiUrl(`suoritusjakoV3`), { body: { secret: id } })
  )

export const fetchSuoritetutTutkinnot = (id: string) =>
  handleExpiredSession(
    apiGet<SuoritetutTutkinnotOppijaJakolinkillä>(
      apiUrl(`opinnot/suoritetut-tutkinnot/${id}`)
    )
  )

export const fetchAktiivisetJaPäättyneetOpinnot = (id: string) =>
  handleExpiredSession(
    apiGet<AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä>(
      apiUrl(`opinnot/aktiiviset-ja-paattyneet-opinnot/${id}`)
    )
  )

export type SuoritusjakoTehty = {
  tehty: boolean
}

export const fetchSuoritusjakoTehty = (
  opiskeluoikeusOid: string,
  suorituksenTyyppi?: string
) =>
  handleExpiredSession(
    apiPost<SuoritusjakoTehty>(
      apiUrl(
        `opiskeluoikeus/suostumuksenperuutus/suoritusjakoTehty/${opiskeluoikeusOid}`,
        { suorituksentyyppi: suorituksenTyyppi }
      )
    )
  )

export const peruutaSuostumus = (
  opiskeluoikeusOid: string,
  suorituksenTyyppi?: string
) =>
  handleExpiredSession(
    apiPost<SuoritusjakoTehty>(
      apiUrl(`opiskeluoikeus/suostumuksenperuutus/${opiskeluoikeusOid}`, {
        suorituksentyyppi: suorituksenTyyppi
      })
    )
  )

export const fetchYoTodistusState = (oppijaOid: string, language: string) =>
  apiGet<YtrCertificateResponse>(
    apiUrl(`yotodistus/status/${language}/${oppijaOid}`)
  )

export const generateYoTodistus = (oppijaOid: string, language: string) =>
  handleExpiredSession(
    apiGet<void>(apiUrl(`yotodistus/generate/${language}/${oppijaOid}`))
  )

export const fetchOsaamismerkkikuva = (koodiarvo: string) =>
  handleExpiredSession(
    apiGet<Osaamismerkkikuva>(
      apiUrl(`osaamismerkkiperusteet/kuva/${koodiarvo}`)
    )
  )

export const fetchOrganisaationOpiskeluoikeustyypit = <
  T extends string = string
>(
  oid: string
) =>
  handleExpiredSession(
    apiGet<Koodistokoodiviite<T>[]>(
      apiUrl(`oppilaitos/opiskeluoikeustyypit/${oid}`)
    )
  )

export const fetchOpiskeluoikeusClassMapping = () =>
  handleExpiredSession(
    apiGet<OpiskeluoikeusClass[]>(apiUrl('types/opiskeluoikeustyypit'))
  )

export const fetchSuoritusPrefill = (
  koodistoUri: string,
  koodiarvo: string,
  tyyppi?: string,
  toimintaAlueittain?: boolean
) =>
  handleExpiredSession(
    apiGet<Suoritus[]>(
      apiUrl(`types/prefill/suoritukset/${koodistoUri}/${koodiarvo}`, {
        tyyppi,
        toimintaAlueittain
      })
    )
  )

export const fetchTutkinnonPerusteenSuoritustavat = (diaarinumero: string) =>
  handleExpiredSession(
    apiGet<Koodistokoodiviite<'ammatillisentutkinnonsuoritustapa'>[]>(
      apiUrl(`tutkinnonperusteet/suoritustavat/${diaarinumero}`)
    )
  )

export const fetchOppiaineenKurssit = (
  oppiaineKoodistoUri: string,
  oppiaineKoodiarvo: string,
  kurssikoodistoUris: string[],
  oppimääräKoodistoUri?: string,
  oppimääräKoodiarvo?: string,
  oppimääränDiaarinumero?: string
) =>
  handleExpiredSession(
    apiGet<TypedEnumValue<Koodistokoodiviite>[]>(
      apiUrl(
        `editor/koodit/${oppiaineKoodistoUri}/${oppiaineKoodiarvo}/kurssit/${kurssikoodistoUris.join(',')}`,
        {
          oppimaaraKoodisto: oppimääräKoodistoUri,
          oppimaaraKoodiarvo: oppimääräKoodiarvo,
          oppimaaraDiaarinumero: oppimääränDiaarinumero
        }
      )
    )
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
