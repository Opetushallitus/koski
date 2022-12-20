import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 * Sisältää myös tiedon opintojen rahoituksesta jaksoittain
 *
 * @see `fi.oph.koski.schema.AmmatillinenOpiskeluoikeusjakso`
 */
export type AmmatillinenOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.schema.AmmatillinenOpiskeluoikeusjakso'
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'loma'
    | 'eronnut'
    | 'peruutettu'
    | 'katsotaaneronneeksi'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
  >
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', string>
}

export const AmmatillinenOpiskeluoikeusjakso = (o: {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'loma'
    | 'eronnut'
    | 'peruutettu'
    | 'katsotaaneronneeksi'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
  >
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', string>
}): AmmatillinenOpiskeluoikeusjakso => ({
  $class: 'fi.oph.koski.schema.AmmatillinenOpiskeluoikeusjakso',
  ...o
})

export const isAmmatillinenOpiskeluoikeusjakso = (
  a: any
): a is AmmatillinenOpiskeluoikeusjakso =>
  a?.$class === 'fi.oph.koski.schema.AmmatillinenOpiskeluoikeusjakso'
