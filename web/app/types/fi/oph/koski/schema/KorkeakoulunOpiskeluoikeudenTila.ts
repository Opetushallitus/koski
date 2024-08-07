import { KorkeakoulunOpiskeluoikeusjakso } from './KorkeakoulunOpiskeluoikeusjakso'

/**
 * KorkeakoulunOpiskeluoikeudenTila
 *
 * @see `fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenTila`
 */
export type KorkeakoulunOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<KorkeakoulunOpiskeluoikeusjakso>
}

export const KorkeakoulunOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<KorkeakoulunOpiskeluoikeusjakso>
  } = {}
): KorkeakoulunOpiskeluoikeudenTila => ({
  $class: 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

KorkeakoulunOpiskeluoikeudenTila.className =
  'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenTila' as const

export const isKorkeakoulunOpiskeluoikeudenTila = (
  a: any
): a is KorkeakoulunOpiskeluoikeudenTila =>
  a?.$class === 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenTila'
