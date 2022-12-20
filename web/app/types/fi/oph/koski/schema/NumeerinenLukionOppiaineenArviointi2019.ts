import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * NumeerinenLukionOppiaineenArviointi2019
 *
 * @see `fi.oph.koski.schema.NumeerinenLukionOppiaineenArviointi2019`
 */
export type NumeerinenLukionOppiaineenArviointi2019 = {
  $class: 'fi.oph.koski.schema.NumeerinenLukionOppiaineenArviointi2019'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  päivä?: string
  hyväksytty?: boolean
}

export const NumeerinenLukionOppiaineenArviointi2019 = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  päivä?: string
  hyväksytty?: boolean
}): NumeerinenLukionOppiaineenArviointi2019 => ({
  $class: 'fi.oph.koski.schema.NumeerinenLukionOppiaineenArviointi2019',
  ...o
})

export const isNumeerinenLukionOppiaineenArviointi2019 = (
  a: any
): a is NumeerinenLukionOppiaineenArviointi2019 =>
  a?.$class === 'fi.oph.koski.schema.NumeerinenLukionOppiaineenArviointi2019'
