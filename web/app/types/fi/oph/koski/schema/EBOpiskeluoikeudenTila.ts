import { EBOpiskeluoikeusjakso } from './EBOpiskeluoikeusjakso'

/**
 * EBOpiskeluoikeudenTila
 *
 * @see `fi.oph.koski.schema.EBOpiskeluoikeudenTila`
 */
export type EBOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.schema.EBOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<EBOpiskeluoikeusjakso>
}

export const EBOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<EBOpiskeluoikeusjakso>
  } = {}
): EBOpiskeluoikeudenTila => ({
  $class: 'fi.oph.koski.schema.EBOpiskeluoikeudenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

EBOpiskeluoikeudenTila.className =
  'fi.oph.koski.schema.EBOpiskeluoikeudenTila' as const

export const isEBOpiskeluoikeudenTila = (a: any): a is EBOpiskeluoikeudenTila =>
  a?.$class === 'fi.oph.koski.schema.EBOpiskeluoikeudenTila'
