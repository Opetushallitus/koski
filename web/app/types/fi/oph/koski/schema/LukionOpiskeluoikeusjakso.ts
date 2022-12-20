import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.LukionOpiskeluoikeusjakso`
 */
export type LukionOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.schema.LukionOpiskeluoikeusjakso'
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

export const LukionOpiskeluoikeusjakso = (o: {
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
}): LukionOpiskeluoikeusjakso => ({
  $class: 'fi.oph.koski.schema.LukionOpiskeluoikeusjakso',
  ...o
})

export const isLukionOpiskeluoikeusjakso = (
  a: any
): a is LukionOpiskeluoikeusjakso =>
  a?.$class === 'fi.oph.koski.schema.LukionOpiskeluoikeusjakso'
