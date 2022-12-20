import { TaiteenPerusopetuksenOpiskeluoikeusjakso } from './TaiteenPerusopetuksenOpiskeluoikeusjakso'

/**
 * TaiteenPerusopetuksenOpiskeluoikeudenTila
 *
 * @see `fi.oph.koski.schema.TaiteenPerusopetuksenOpiskeluoikeudenTila`
 */
export type TaiteenPerusopetuksenOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<TaiteenPerusopetuksenOpiskeluoikeusjakso>
}

export const TaiteenPerusopetuksenOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<TaiteenPerusopetuksenOpiskeluoikeusjakso>
  } = {}
): TaiteenPerusopetuksenOpiskeluoikeudenTila => ({
  $class: 'fi.oph.koski.schema.TaiteenPerusopetuksenOpiskeluoikeudenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

export const isTaiteenPerusopetuksenOpiskeluoikeudenTila = (
  a: any
): a is TaiteenPerusopetuksenOpiskeluoikeudenTila =>
  a?.$class === 'fi.oph.koski.schema.TaiteenPerusopetuksenOpiskeluoikeudenTila'
