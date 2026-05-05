import { AhvenanmaanPerusopetuksenOpiskeluoikeusjakso } from './AhvenanmaanPerusopetuksenOpiskeluoikeusjakso'

/**
 * AhvenanmaanPerusopetuksenOpiskeluoikeudenTila
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeudenTila`
 */
export type AhvenanmaanPerusopetuksenOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<AhvenanmaanPerusopetuksenOpiskeluoikeusjakso>
}

export const AhvenanmaanPerusopetuksenOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<AhvenanmaanPerusopetuksenOpiskeluoikeusjakso>
  } = {}
): AhvenanmaanPerusopetuksenOpiskeluoikeudenTila => ({
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeudenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

AhvenanmaanPerusopetuksenOpiskeluoikeudenTila.className =
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeudenTila' as const

export const isAhvenanmaanPerusopetuksenOpiskeluoikeudenTila = (
  a: any
): a is AhvenanmaanPerusopetuksenOpiskeluoikeudenTila =>
  a?.$class ===
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOpiskeluoikeudenTila'
