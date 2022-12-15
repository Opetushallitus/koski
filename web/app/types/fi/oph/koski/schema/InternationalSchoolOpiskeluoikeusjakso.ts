import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.InternationalSchoolOpiskeluoikeusjakso`
 */
export type InternationalSchoolOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.schema.InternationalSchoolOpiskeluoikeusjakso'
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'eronnut'
    | 'peruutettu'
    | 'katsotaaneronneeksi'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
  >
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '1' | '6'>
}

export const InternationalSchoolOpiskeluoikeusjakso = (o: {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'eronnut'
    | 'peruutettu'
    | 'katsotaaneronneeksi'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
  >
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '1' | '6'>
}): InternationalSchoolOpiskeluoikeusjakso => ({
  $class: 'fi.oph.koski.schema.InternationalSchoolOpiskeluoikeusjakso',
  ...o
})

export const isInternationalSchoolOpiskeluoikeusjakso = (
  a: any
): a is InternationalSchoolOpiskeluoikeusjakso =>
  a?.$class === 'InternationalSchoolOpiskeluoikeusjakso'
