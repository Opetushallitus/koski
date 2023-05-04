import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * IBOppiaineenPredictedArviointi
 *
 * @see `fi.oph.koski.schema.IBOppiaineenPredictedArviointi`
 */
export type IBOppiaineenPredictedArviointi = {
  $class: 'fi.oph.koski.schema.IBOppiaineenPredictedArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkoib', string>
  päivä?: string
  hyväksytty?: boolean
}

export const IBOppiaineenPredictedArviointi = (o: {
  arvosana: Koodistokoodiviite<'arviointiasteikkoib', string>
  päivä?: string
  hyväksytty?: boolean
}): IBOppiaineenPredictedArviointi => ({
  $class: 'fi.oph.koski.schema.IBOppiaineenPredictedArviointi',
  ...o
})

IBOppiaineenPredictedArviointi.className =
  'fi.oph.koski.schema.IBOppiaineenPredictedArviointi' as const

export const isIBOppiaineenPredictedArviointi = (
  a: any
): a is IBOppiaineenPredictedArviointi =>
  a?.$class === 'fi.oph.koski.schema.IBOppiaineenPredictedArviointi'
