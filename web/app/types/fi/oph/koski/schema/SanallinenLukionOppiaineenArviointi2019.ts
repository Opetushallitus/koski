import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * SanallinenLukionOppiaineenArviointi2019
 *
 * @see `fi.oph.koski.schema.SanallinenLukionOppiaineenArviointi2019`
 */
export type SanallinenLukionOppiaineenArviointi2019 = {
  $class: 'fi.oph.koski.schema.SanallinenLukionOppiaineenArviointi2019'
  arvosana: Koodistokoodiviite<'arviointiasteikkoyleissivistava', 'H' | 'S'>
  päivä?: string
  hyväksytty?: boolean
}

export const SanallinenLukionOppiaineenArviointi2019 = (o: {
  arvosana: Koodistokoodiviite<'arviointiasteikkoyleissivistava', 'H' | 'S'>
  päivä?: string
  hyväksytty?: boolean
}): SanallinenLukionOppiaineenArviointi2019 => ({
  $class: 'fi.oph.koski.schema.SanallinenLukionOppiaineenArviointi2019',
  ...o
})

SanallinenLukionOppiaineenArviointi2019.className =
  'fi.oph.koski.schema.SanallinenLukionOppiaineenArviointi2019' as const

export const isSanallinenLukionOppiaineenArviointi2019 = (
  a: any
): a is SanallinenLukionOppiaineenArviointi2019 =>
  a?.$class === 'fi.oph.koski.schema.SanallinenLukionOppiaineenArviointi2019'
