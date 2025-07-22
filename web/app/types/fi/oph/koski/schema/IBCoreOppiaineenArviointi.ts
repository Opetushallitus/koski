import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * IBCoreOppiaineenArviointi
 *
 * @see `fi.oph.koski.schema.IBCoreOppiaineenArviointi`
 */
export type IBCoreOppiaineenArviointi = {
  $class: 'fi.oph.koski.schema.IBCoreOppiaineenArviointi'
  päivä?: string
  effort?: Koodistokoodiviite<'effortasteikkoib', string>
  arvosana: Koodistokoodiviite<'arviointiasteikkocorerequirementsib', string>
  predicted?: boolean
  hyväksytty?: boolean
}

export const IBCoreOppiaineenArviointi = (o: {
  päivä?: string
  effort?: Koodistokoodiviite<'effortasteikkoib', string>
  arvosana: Koodistokoodiviite<'arviointiasteikkocorerequirementsib', string>
  predicted?: boolean
  hyväksytty?: boolean
}): IBCoreOppiaineenArviointi => ({
  $class: 'fi.oph.koski.schema.IBCoreOppiaineenArviointi',
  ...o
})

IBCoreOppiaineenArviointi.className =
  'fi.oph.koski.schema.IBCoreOppiaineenArviointi' as const

export const isIBCoreOppiaineenArviointi = (
  a: any
): a is IBCoreOppiaineenArviointi =>
  a?.$class === 'fi.oph.koski.schema.IBCoreOppiaineenArviointi'
