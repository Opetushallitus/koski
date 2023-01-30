import { LocalizedString } from './LocalizedString'

/**
 * Tiedot aiemmin hankitun osaamisen tunnustamisesta.
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen`
 */
export type VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen'
  selite: LocalizedString
}

export const VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen =
  (o: {
    selite: LocalizedString
  }): VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen => ({
    $class:
      'fi.oph.koski.schema.VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen',
    ...o
  })

VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen.className =
  'fi.oph.koski.schema.VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen' as const

export const isVapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen = (
  a: any
): a is VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen =>
  a?.$class ===
  'fi.oph.koski.schema.VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen'
