import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * PassFailOppiaineenArviointi
 *
 * @see `fi.oph.koski.schema.PassFailOppiaineenArviointi`
 */
export type PassFailOppiaineenArviointi = {
  $class: 'fi.oph.koski.schema.PassFailOppiaineenArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkointernationalschool',
    'pass' | 'fail'
  >
  päivä?: string
  hyväksytty?: boolean
}

export const PassFailOppiaineenArviointi = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkointernationalschool',
    'pass' | 'fail'
  >
  päivä?: string
  hyväksytty?: boolean
}): PassFailOppiaineenArviointi => ({
  $class: 'fi.oph.koski.schema.PassFailOppiaineenArviointi',
  ...o
})

export const isPassFailOppiaineenArviointi = (
  a: any
): a is PassFailOppiaineenArviointi =>
  a?.$class === 'PassFailOppiaineenArviointi'
