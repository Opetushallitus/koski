import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso`
 */
export type VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso'
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    'hyvaksytystisuoritettu' | 'mitatoity'
  >
}

export const VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso = (o: {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    'hyvaksytystisuoritettu' | 'mitatoity'
  >
}): VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso => ({
  $class:
    'fi.oph.koski.schema.VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso',
  ...o
})

VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso.className =
  'fi.oph.koski.schema.VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso' as const

export const isVapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso = (
  a: any
): a is VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso =>
  a?.$class ===
  'fi.oph.koski.schema.VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso'
