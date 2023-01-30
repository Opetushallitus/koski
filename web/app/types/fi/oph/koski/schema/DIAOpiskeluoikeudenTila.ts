import { DIAOpiskeluoikeusjakso } from './DIAOpiskeluoikeusjakso'

/**
 * DIAOpiskeluoikeudenTila
 *
 * @see `fi.oph.koski.schema.DIAOpiskeluoikeudenTila`
 */
export type DIAOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.schema.DIAOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<DIAOpiskeluoikeusjakso>
}

export const DIAOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<DIAOpiskeluoikeusjakso>
  } = {}
): DIAOpiskeluoikeudenTila => ({
  $class: 'fi.oph.koski.schema.DIAOpiskeluoikeudenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

DIAOpiskeluoikeudenTila.className =
  'fi.oph.koski.schema.DIAOpiskeluoikeudenTila' as const

export const isDIAOpiskeluoikeudenTila = (
  a: any
): a is DIAOpiskeluoikeudenTila =>
  a?.$class === 'fi.oph.koski.schema.DIAOpiskeluoikeudenTila'
