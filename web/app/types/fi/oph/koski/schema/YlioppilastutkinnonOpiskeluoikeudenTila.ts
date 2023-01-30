import { LukionOpiskeluoikeusjakso } from './LukionOpiskeluoikeusjakso'

/**
 * YlioppilastutkinnonOpiskeluoikeudenTila
 *
 * @see `fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeudenTila`
 */
export type YlioppilastutkinnonOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<LukionOpiskeluoikeusjakso>
}

export const YlioppilastutkinnonOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<LukionOpiskeluoikeusjakso>
  } = {}
): YlioppilastutkinnonOpiskeluoikeudenTila => ({
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeudenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

YlioppilastutkinnonOpiskeluoikeudenTila.className =
  'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeudenTila' as const

export const isYlioppilastutkinnonOpiskeluoikeudenTila = (
  a: any
): a is YlioppilastutkinnonOpiskeluoikeudenTila =>
  a?.$class === 'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeudenTila'
