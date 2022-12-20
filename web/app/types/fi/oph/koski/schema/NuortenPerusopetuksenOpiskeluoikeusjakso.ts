import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.NuortenPerusopetuksenOpiskeluoikeusjakso`
 */
export type NuortenPerusopetuksenOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenOpiskeluoikeusjakso'
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
}

export const NuortenPerusopetuksenOpiskeluoikeusjakso = (o: {
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
}): NuortenPerusopetuksenOpiskeluoikeusjakso => ({
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenOpiskeluoikeusjakso',
  ...o
})

export const isNuortenPerusopetuksenOpiskeluoikeusjakso = (
  a: any
): a is NuortenPerusopetuksenOpiskeluoikeusjakso =>
  a?.$class === 'fi.oph.koski.schema.NuortenPerusopetuksenOpiskeluoikeusjakso'
