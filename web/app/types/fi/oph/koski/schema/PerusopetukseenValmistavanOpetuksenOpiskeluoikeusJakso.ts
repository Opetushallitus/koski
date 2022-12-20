import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso`
 */
export type PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso = {
  $class: 'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso'
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
}

export const PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso = (o: {
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
}): PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso => ({
  $class:
    'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso',
  ...o
})

export const isPerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso = (
  a: any
): a is PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso =>
  a?.$class ===
  'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso'
