import { apiDelete, ApiFailure, apiGet, apiPut } from '../api-fetch'
import { HenkilönOpiskeluoikeusVersiot } from '../types/fi/oph/koski/oppija/HenkilonOpiskeluoikeusVersiot'
import { OrganisaatioHierarkia } from '../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { KeyValue } from '../types/fi/oph/koski/preferences/KeyValue'
import { OidHenkilö } from '../types/fi/oph/koski/schema/OidHenkilo'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'
import { Oppija } from '../types/fi/oph/koski/schema/Oppija'
import { StorablePreference } from '../types/fi/oph/koski/schema/StorablePreference'
import { Constraint } from '../types/fi/oph/koski/typemodel/Constraint'
import { GroupedKoodistot } from '../types/fi/oph/koski/typemodel/GroupedKoodistot'
import { tapLeftP } from './fp/either'
import { queryString } from './url'

const apiUrl = (path: string, query?: object): string =>
  `/koski/api/${path}${queryString({ class_refs: 'true', ...query })}`

export const fetchOppija = (oppijaOid: String) =>
  handleExpiredSession(apiGet<Oppija>(apiUrl(`oppija/${oppijaOid}`)))

export const fetchOpiskeluoikeus = (opiskeluoikeusOid: string) =>
  handleExpiredSession(
    apiGet<Opiskeluoikeus>(apiUrl(`opiskeluoikeus/${opiskeluoikeusOid}`))
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

// Virhetilanteiden hallinta

const handleExpiredSession = tapLeftP((failure: ApiFailure) => {
  if (failure.status === 401) {
    // 401 Unauthorized -> käyttäjä ei ole (enää) kirjautunut -> lähetä kirjautumiseen lataamalla sivu uudelleen.
    // Kirjautuminen ei laukea siitä, että käyttäjä yritti nähdä tietoa, johon hänellä ei ole oikeutta,
    // koska Valpas-APIt palauttavat siinä tapauksessa 403 Forbidden.
    location.reload()
  }
})
