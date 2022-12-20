import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * SanallinenLukionArviointi
 *
 * @see `fi.oph.koski.schema.SanallinenLukionArviointi`
 */
export type SanallinenLukionArviointi = {
  $class: 'fi.oph.koski.schema.SanallinenLukionArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'S' | 'H' | 'O'
  >
  kuvaus?: LocalizedString
  päivä: string
  hyväksytty?: boolean
}

export const SanallinenLukionArviointi = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'S' | 'H' | 'O'
  >
  kuvaus?: LocalizedString
  päivä: string
  hyväksytty?: boolean
}): SanallinenLukionArviointi => ({
  $class: 'fi.oph.koski.schema.SanallinenLukionArviointi',
  ...o
})

export const isSanallinenLukionArviointi = (
  a: any
): a is SanallinenLukionArviointi =>
  a?.$class === 'fi.oph.koski.schema.SanallinenLukionArviointi'
