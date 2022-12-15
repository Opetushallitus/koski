import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso`
 */
export type EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso'
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'eronnut'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
  >
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '6'>
}

export const EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso = (o: {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'eronnut'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
  >
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '6'>
}): EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso => ({
  $class: 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso',
  ...o
})

export const isEuropeanSchoolOfHelsinkiOpiskeluoikeusjakso = (
  a: any
): a is EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso =>
  a?.$class === 'EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso'
