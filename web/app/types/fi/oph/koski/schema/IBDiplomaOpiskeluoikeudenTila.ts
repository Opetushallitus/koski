import { IBDiplomaOpiskeluoikeusjakso } from './IBDiplomaOpiskeluoikeusjakso'

/**
 * IBDiplomaOpiskeluoikeudenTila
 *
 * @see `fi.oph.koski.schema.IBDiplomaOpiskeluoikeudenTila`
 */
export type IBDiplomaOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.schema.IBDiplomaOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<IBDiplomaOpiskeluoikeusjakso>
}

export const IBDiplomaOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<IBDiplomaOpiskeluoikeusjakso>
  } = {}
): IBDiplomaOpiskeluoikeudenTila => ({
  $class: 'fi.oph.koski.schema.IBDiplomaOpiskeluoikeudenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

IBDiplomaOpiskeluoikeudenTila.className =
  'fi.oph.koski.schema.IBDiplomaOpiskeluoikeudenTila' as const

export const isIBDiplomaOpiskeluoikeudenTila = (
  a: any
): a is IBDiplomaOpiskeluoikeudenTila =>
  a?.$class === 'fi.oph.koski.schema.IBDiplomaOpiskeluoikeudenTila'
