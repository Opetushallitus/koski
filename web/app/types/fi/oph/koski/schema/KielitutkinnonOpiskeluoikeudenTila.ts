import { KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso } from './KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso'

/**
 * KielitutkinnonOpiskeluoikeudenTila
 *
 * @see `fi.oph.koski.schema.KielitutkinnonOpiskeluoikeudenTila`
 */
export type KielitutkinnonOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.schema.KielitutkinnonOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso>
}

export const KielitutkinnonOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso>
  } = {}
): KielitutkinnonOpiskeluoikeudenTila => ({
  $class: 'fi.oph.koski.schema.KielitutkinnonOpiskeluoikeudenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

KielitutkinnonOpiskeluoikeudenTila.className =
  'fi.oph.koski.schema.KielitutkinnonOpiskeluoikeudenTila' as const

export const isKielitutkinnonOpiskeluoikeudenTila = (
  a: any
): a is KielitutkinnonOpiskeluoikeudenTila =>
  a?.$class === 'fi.oph.koski.schema.KielitutkinnonOpiskeluoikeudenTila'
