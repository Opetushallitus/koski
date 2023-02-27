import { YlioppilastutkinnonOpiskeluoikeusjakso } from './YlioppilastutkinnonOpiskeluoikeusjakso'

/**
 * YlioppilastutkinnonOpiskeluoikeudenTila
 *
 * @see `fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeudenTila`
 */
export type YlioppilastutkinnonOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.schema.YlioppilastutkinnonOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<YlioppilastutkinnonOpiskeluoikeusjakso>
}

export const YlioppilastutkinnonOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<YlioppilastutkinnonOpiskeluoikeusjakso>
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
