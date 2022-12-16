import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeusjakso`
 */
export type AikuistenPerusopetuksenOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeusjakso'
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

export const AikuistenPerusopetuksenOpiskeluoikeusjakso = (o: {
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
}): AikuistenPerusopetuksenOpiskeluoikeusjakso => ({
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeusjakso',
  ...o
})

export const isAikuistenPerusopetuksenOpiskeluoikeusjakso = (
  a: any
): a is AikuistenPerusopetuksenOpiskeluoikeusjakso =>
  a?.$class === 'AikuistenPerusopetuksenOpiskeluoikeusjakso'
