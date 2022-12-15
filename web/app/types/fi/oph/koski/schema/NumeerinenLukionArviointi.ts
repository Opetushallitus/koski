import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * NumeerinenLukionArviointi
 *
 * @see `fi.oph.koski.schema.NumeerinenLukionArviointi`
 */
export type NumeerinenLukionArviointi = {
  $class: 'fi.oph.koski.schema.NumeerinenLukionArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  päivä: string
  hyväksytty?: boolean
}

export const NumeerinenLukionArviointi = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  päivä: string
  hyväksytty?: boolean
}): NumeerinenLukionArviointi => ({
  $class: 'fi.oph.koski.schema.NumeerinenLukionArviointi',
  ...o
})

export const isNumeerinenLukionArviointi = (
  a: any
): a is NumeerinenLukionArviointi => a?.$class === 'NumeerinenLukionArviointi'
