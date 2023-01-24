import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Arvioitsija } from './Arvioitsija'

/**
 * SecondaryS7PreliminaryMarkArviointi
 *
 * @see `fi.oph.koski.schema.SecondaryS7PreliminaryMarkArviointi`
 */
export type SecondaryS7PreliminaryMarkArviointi = {
  $class: 'fi.oph.koski.schema.SecondaryS7PreliminaryMarkArviointi'
  päivä?: string
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkis7preliminarymark',
    string
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  arvioitsijat?: Array<Arvioitsija>
}

export const SecondaryS7PreliminaryMarkArviointi = (o: {
  päivä?: string
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkis7preliminarymark',
    string
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  arvioitsijat?: Array<Arvioitsija>
}): SecondaryS7PreliminaryMarkArviointi => ({
  $class: 'fi.oph.koski.schema.SecondaryS7PreliminaryMarkArviointi',
  ...o
})

SecondaryS7PreliminaryMarkArviointi.className =
  'fi.oph.koski.schema.SecondaryS7PreliminaryMarkArviointi' as const

export const isSecondaryS7PreliminaryMarkArviointi = (
  a: any
): a is SecondaryS7PreliminaryMarkArviointi =>
  a?.$class === 'fi.oph.koski.schema.SecondaryS7PreliminaryMarkArviointi'
