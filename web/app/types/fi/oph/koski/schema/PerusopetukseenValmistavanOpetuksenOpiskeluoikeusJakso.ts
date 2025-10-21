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
    | 'eronnut'
    | 'peruutettu'
    | 'katsotaaneronneeksi'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
    | 'loma'
  >
}

export const PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso = (o: {
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
    | 'loma'
  >
}): PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso => ({
  $class:
    'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso',
  ...o
})

PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso.className =
  'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso' as const

export const isPerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso = (
  a: any
): a is PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso =>
  a?.$class ===
  'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso'
