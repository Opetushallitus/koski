import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Arvioitsija } from './Arvioitsija'

/**
 * SecondaryNumericalMarkArviointi
 *
 * @see `fi.oph.koski.schema.SecondaryNumericalMarkArviointi`
 */
export type SecondaryNumericalMarkArviointi = {
  $class: 'fi.oph.koski.schema.SecondaryNumericalMarkArviointi'
  päivä?: string
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkinumericalmark',
    string
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  arvioitsijat?: Array<Arvioitsija>
}

export const SecondaryNumericalMarkArviointi = (o: {
  päivä?: string
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkinumericalmark',
    string
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  arvioitsijat?: Array<Arvioitsija>
}): SecondaryNumericalMarkArviointi => ({
  $class: 'fi.oph.koski.schema.SecondaryNumericalMarkArviointi',
  ...o
})

SecondaryNumericalMarkArviointi.className =
  'fi.oph.koski.schema.SecondaryNumericalMarkArviointi' as const

export const isSecondaryNumericalMarkArviointi = (
  a: any
): a is SecondaryNumericalMarkArviointi =>
  a?.$class === 'fi.oph.koski.schema.SecondaryNumericalMarkArviointi'
