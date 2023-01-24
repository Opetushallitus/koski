import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.DIAOpiskeluoikeusjakso`
 */
export type DIAOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.schema.DIAOpiskeluoikeusjakso'
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

export const DIAOpiskeluoikeusjakso = (o: {
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
}): DIAOpiskeluoikeusjakso => ({
  $class: 'fi.oph.koski.schema.DIAOpiskeluoikeusjakso',
  ...o
})

DIAOpiskeluoikeusjakso.className =
  'fi.oph.koski.schema.DIAOpiskeluoikeusjakso' as const

export const isDIAOpiskeluoikeusjakso = (a: any): a is DIAOpiskeluoikeusjakso =>
  a?.$class === 'fi.oph.koski.schema.DIAOpiskeluoikeusjakso'
