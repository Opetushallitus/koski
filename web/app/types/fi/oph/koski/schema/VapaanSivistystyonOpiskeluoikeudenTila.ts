import { VapaanSivistystyönOpiskeluoikeusjakso } from './VapaanSivistystyonOpiskeluoikeusjakso'

/**
 * VapaanSivistystyönOpiskeluoikeudenTila
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönOpiskeluoikeudenTila`
 */
export type VapaanSivistystyönOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<VapaanSivistystyönOpiskeluoikeusjakso>
}

export const VapaanSivistystyönOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<VapaanSivistystyönOpiskeluoikeusjakso>
  } = {}
): VapaanSivistystyönOpiskeluoikeudenTila => ({
  $class: 'fi.oph.koski.schema.VapaanSivistystyönOpiskeluoikeudenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

export const isVapaanSivistystyönOpiskeluoikeudenTila = (
  a: any
): a is VapaanSivistystyönOpiskeluoikeudenTila =>
  a?.$class === 'VapaanSivistystyönOpiskeluoikeudenTila'
