import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.AhvenanmaanAmmatillinenOpiskeluoikeusjakso`
 */
export type AhvenanmaanAmmatillinenOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.schema.AhvenanmaanAmmatillinenOpiskeluoikeusjakso'
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'katsotaaneronneeksi'
    | 'lasna'
    | 'mitatoity'
    | 'peruutettu'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
  >
}

export const AhvenanmaanAmmatillinenOpiskeluoikeusjakso = (o: {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'katsotaaneronneeksi'
    | 'lasna'
    | 'mitatoity'
    | 'peruutettu'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
  >
}): AhvenanmaanAmmatillinenOpiskeluoikeusjakso => ({
  $class: 'fi.oph.koski.schema.AhvenanmaanAmmatillinenOpiskeluoikeusjakso',
  ...o
})

AhvenanmaanAmmatillinenOpiskeluoikeusjakso.className =
  'fi.oph.koski.schema.AhvenanmaanAmmatillinenOpiskeluoikeusjakso' as const

export const isAhvenanmaanAmmatillinenOpiskeluoikeusjakso = (
  a: any
): a is AhvenanmaanAmmatillinenOpiskeluoikeusjakso =>
  a?.$class === 'fi.oph.koski.schema.AhvenanmaanAmmatillinenOpiskeluoikeusjakso'
