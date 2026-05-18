import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeusjakso`
 */
export type AhvenanmaanPerusopetuksenOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeusjakso'
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'eronnut'
    | 'katsotaaneronneeksi'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
  >
}

export const AhvenanmaanPerusopetuksenOpiskeluoikeusjakso = (o: {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'eronnut'
    | 'katsotaaneronneeksi'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
  >
}): AhvenanmaanPerusopetuksenOpiskeluoikeusjakso => ({
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeusjakso',
  ...o
})

AhvenanmaanPerusopetuksenOpiskeluoikeusjakso.className =
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeusjakso' as const

export const isAhvenanmaanPerusopetuksenOpiskeluoikeusjakso = (
  a: any
): a is AhvenanmaanPerusopetuksenOpiskeluoikeusjakso =>
  a?.$class ===
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeusjakso'
