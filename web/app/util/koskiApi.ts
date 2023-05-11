import { apiDelete, ApiFailure, apiGet, apiPost, apiPut } from '../api-fetch'
import { OpiskeluoikeusHistoryPatch } from '../types/fi/oph/koski/history/OpiskeluoikeusHistoryPatch'
import { HenkilönOpiskeluoikeusVersiot } from '../types/fi/oph/koski/oppija/HenkilonOpiskeluoikeusVersiot'
import { OrganisaatioHierarkia } from '../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { KeyValue } from '../types/fi/oph/koski/preferences/KeyValue'
import { OidHenkilö } from '../types/fi/oph/koski/schema/OidHenkilo'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'
import { Oppija } from '../types/fi/oph/koski/schema/Oppija'
import { PäätasonSuoritus } from '../types/fi/oph/koski/schema/PaatasonSuoritus'
import { StorablePreference } from '../types/fi/oph/koski/schema/StorablePreference'
import { Constraint } from '../types/fi/oph/koski/typemodel/Constraint'
import { GroupedKoodistot } from '../types/fi/oph/koski/typemodel/GroupedKoodistot'
import { YtrCertificateResponse } from '../types/fi/oph/koski/ytr/YtrCertificateResponse'
import { tapLeftP } from './fp/either'
import { queryString } from './url'
import { SuoritetutTutkinnotOppija } from '../types/fi/oph/koski/suoritusjako/suoritetuttutkinnot/SuoritetutTutkinnotOppija'

const apiUrl = (path: string, query?: object): string =>
  `/koski/api/${path}${queryString({ class_refs: 'true', ...query })}`

export const fetchOppija = (oppijaOid: string) =>
  handleExpiredSession(apiGet<Oppija>(apiUrl(`oppija/${oppijaOid}`)))

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

export const fetchConstraint = (schemaClass: string) =>
  handleExpiredSession(
    apiGet<Constraint>(apiUrl(`types/constraints/${schemaClass}`))
  )

export const fetchOrganisaatioHierarkia = () =>
  handleExpiredSession(
    apiGet<OrganisaatioHierarkia[]>(apiUrl(`organisaatio/hierarkia`))
  )

export const queryOrganisaatioHierarkia = (query: string) =>
  handleExpiredSession(
    apiGet<OrganisaatioHierarkia[]>(apiUrl(`organisaatio/hierarkia`, { query }))
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

export const fetchOmatTiedotOppija = () =>
  handleExpiredSession(apiGet<Oppija>(apiUrl('omattiedotV2/oppija')))

export const fetchSuoritusjako = (id: string) =>
  handleExpiredSession(
    apiPost<Oppija>(apiUrl(`suoritusjakoV3`), { body: { secret: id } })
  )

export const fetchSuoritetutTutkinnot = (id: string) =>
  handleExpiredSession(
    apiGet<SuoritetutTutkinnotOppija>(
      apiUrl(`opinnot/suoritetut-tutkinnot/${id}`)
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
  handleExpiredSession(
    apiGet<YtrCertificateResponse>(
      apiUrl(`yotodistus/status/${language}/${oppijaOid}`)
    )
  )

export const generateYoTodistus = (oppijaOid: string, language: string) =>
  handleExpiredSession(
    apiGet<void>(apiUrl(`yotodistus/generate/${language}/${oppijaOid}`))
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
