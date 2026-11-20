import { AhvenanmaanAmmatillinenOpiskeluoikeusjakso } from './AhvenanmaanAmmatillinenOpiskeluoikeusjakso'

/**
 * AhvenanmaanAmmatillinenOpiskeluoikeudenTila
 *
 * @see `fi.oph.koski.schema.AhvenanmaanAmmatillinenOpiskeluoikeudenTila`
 */
export type AhvenanmaanAmmatillinenOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.schema.AhvenanmaanAmmatillinenOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<AhvenanmaanAmmatillinenOpiskeluoikeusjakso>
}

export const AhvenanmaanAmmatillinenOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<AhvenanmaanAmmatillinenOpiskeluoikeusjakso>
  } = {}
): AhvenanmaanAmmatillinenOpiskeluoikeudenTila => ({
  $class: 'fi.oph.koski.schema.AhvenanmaanAmmatillinenOpiskeluoikeudenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

AhvenanmaanAmmatillinenOpiskeluoikeudenTila.className =
  'fi.oph.koski.schema.AhvenanmaanAmmatillinenOpiskeluoikeudenTila' as const

export const isAhvenanmaanAmmatillinenOpiskeluoikeudenTila = (
  a: any
): a is AhvenanmaanAmmatillinenOpiskeluoikeudenTila =>
  a?.$class ===
  'fi.oph.koski.schema.AhvenanmaanAmmatillinenOpiskeluoikeudenTila'
