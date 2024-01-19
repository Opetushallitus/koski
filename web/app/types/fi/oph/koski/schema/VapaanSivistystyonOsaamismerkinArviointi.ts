import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * VapaanSivistystyönOsaamismerkinArviointi
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönOsaamismerkinArviointi`
 */
export type VapaanSivistystyönOsaamismerkinArviointi = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönOsaamismerkinArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkovst', 'Hyväksytty'>
  päivä: string
  hyväksytty?: boolean
}

export const VapaanSivistystyönOsaamismerkinArviointi = (o: {
  arvosana?: Koodistokoodiviite<'arviointiasteikkovst', 'Hyväksytty'>
  päivä: string
  hyväksytty?: boolean
}): VapaanSivistystyönOsaamismerkinArviointi => ({
  $class: 'fi.oph.koski.schema.VapaanSivistystyönOsaamismerkinArviointi',
  arvosana: Koodistokoodiviite({
    koodiarvo: 'Hyväksytty',
    koodistoUri: 'arviointiasteikkovst'
  }),
  ...o
})

VapaanSivistystyönOsaamismerkinArviointi.className =
  'fi.oph.koski.schema.VapaanSivistystyönOsaamismerkinArviointi' as const

export const isVapaanSivistystyönOsaamismerkinArviointi = (
  a: any
): a is VapaanSivistystyönOsaamismerkinArviointi =>
  a?.$class === 'fi.oph.koski.schema.VapaanSivistystyönOsaamismerkinArviointi'
