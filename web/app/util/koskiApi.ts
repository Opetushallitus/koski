import { ApiFailure, apiGet, apiPut } from '../api-fetch'
import { HenkilönOpiskeluoikeusVersiot } from '../types/fi/oph/koski/oppija/HenkilonOpiskeluoikeusVersiot'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { OidHenkilö } from '../types/fi/oph/koski/schema/OidHenkilo'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'
import { Oppija } from '../types/fi/oph/koski/schema/Oppija'
import { Constraint } from '../types/fi/oph/koski/typemodel/Constraint'
import { GroupedKoodistot } from '../types/fi/oph/koski/typemodel/GroupedKoodistot'
import { tapLeftP } from './fp/either'

const apiUrl = (path: string, withClasses: boolean = true): string =>
  `/koski/api/${path}${withClasses ? '?class_refs=true' : ''}`

export const fetchOppija = (oppijaOid: String) =>
  handleExpiredSession(apiGet<Oppija>(apiUrl(`oppija/${oppijaOid}`)))

export const fetchOpiskeluoikeus = (opiskeluoikeusOid: string) =>
  handleExpiredSession(
    apiGet<Opiskeluoikeus>(apiUrl(`opiskeluoikeus/${opiskeluoikeusOid}`))
  )

export const saveOpiskeluoikeus =
  (oppijaOid: string) => (opiskeluoikeus: Opiskeluoikeus) =>
    handleExpiredSession(
      apiPut<HenkilönOpiskeluoikeusVersiot>(apiUrl('oppija', false), {
        body: Oppija({
          henkilö: OidHenkilö({ oid: oppijaOid }),
          opiskeluoikeudet: [opiskeluoikeus]
        })
      })
    )

export const fetchKoodistot = (koodistoUris: string[]) =>
  handleExpiredSession(
    apiGet<GroupedKoodistot>(apiUrl(`types/koodisto/${koodistoUris.join(',')}`))
  )

export const fetchConstraint = (schemaClass: string) =>
  handleExpiredSession(
    apiGet<Constraint>(apiUrl(`types/constraints/${schemaClass}`))
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
