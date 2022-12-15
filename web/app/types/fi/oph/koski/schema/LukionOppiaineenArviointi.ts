import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * LukionOppiaineenArviointi
 *
 * @see `fi.oph.koski.schema.LukionOppiaineenArviointi`
 */
export type LukionOppiaineenArviointi = {
  $class: 'fi.oph.koski.schema.LukionOppiaineenArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkoyleissivistava', string>
  päivä?: string
  hyväksytty?: boolean
}

export const LukionOppiaineenArviointi = (o: {
  arvosana: Koodistokoodiviite<'arviointiasteikkoyleissivistava', string>
  päivä?: string
  hyväksytty?: boolean
}): LukionOppiaineenArviointi => ({
  $class: 'fi.oph.koski.schema.LukionOppiaineenArviointi',
  ...o
})

export const isLukionOppiaineenArviointi = (
  a: any
): a is LukionOppiaineenArviointi => a?.$class === 'LukionOppiaineenArviointi'