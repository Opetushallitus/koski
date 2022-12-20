import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * IBOppiaineenArviointi
 *
 * @see `fi.oph.koski.schema.IBOppiaineenArviointi`
 */
export type IBOppiaineenArviointi = {
  $class: 'fi.oph.koski.schema.IBOppiaineenArviointi'
  päivä?: string
  effort?: Koodistokoodiviite<'effortasteikkoib', string>
  arvosana: Koodistokoodiviite<'arviointiasteikkoib', string>
  predicted: boolean
  hyväksytty?: boolean
}

export const IBOppiaineenArviointi = (o: {
  päivä?: string
  effort?: Koodistokoodiviite<'effortasteikkoib', string>
  arvosana: Koodistokoodiviite<'arviointiasteikkoib', string>
  predicted: boolean
  hyväksytty?: boolean
}): IBOppiaineenArviointi => ({
  $class: 'fi.oph.koski.schema.IBOppiaineenArviointi',
  ...o
})

export const isIBOppiaineenArviointi = (a: any): a is IBOppiaineenArviointi =>
  a?.$class === 'fi.oph.koski.schema.IBOppiaineenArviointi'
