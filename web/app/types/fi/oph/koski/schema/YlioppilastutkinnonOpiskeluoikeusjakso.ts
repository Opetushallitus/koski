import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeusjakso`
 */
export type YlioppilastutkinnonOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeusjakso'
  alku: string
  tila: Koodistokoodiviite<'koskiopiskeluoikeudentila', 'valmistunut'>
}

export const YlioppilastutkinnonOpiskeluoikeusjakso = (o: {
  alku: string
  tila?: Koodistokoodiviite<'koskiopiskeluoikeudentila', 'valmistunut'>
}): YlioppilastutkinnonOpiskeluoikeusjakso => ({
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeusjakso',
  tila: Koodistokoodiviite({
    koodiarvo: 'valmistunut',
    koodistoUri: 'koskiopiskeluoikeudentila'
  }),
  ...o
})

YlioppilastutkinnonOpiskeluoikeusjakso.className =
  'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeusjakso' as const

export const isYlioppilastutkinnonOpiskeluoikeusjakso = (
  a: any
): a is YlioppilastutkinnonOpiskeluoikeusjakso =>
  a?.$class === 'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeusjakso'
