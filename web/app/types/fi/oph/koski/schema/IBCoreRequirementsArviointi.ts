import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * IBCoreRequirementsArviointi
 *
 * @see `fi.oph.koski.schema.IBCoreRequirementsArviointi`
 */
export type IBCoreRequirementsArviointi = {
  $class: 'fi.oph.koski.schema.IBCoreRequirementsArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkocorerequirementsib', string>
  predicted: boolean
  päivä?: string
  hyväksytty?: boolean
}

export const IBCoreRequirementsArviointi = (o: {
  arvosana: Koodistokoodiviite<'arviointiasteikkocorerequirementsib', string>
  predicted: boolean
  päivä?: string
  hyväksytty?: boolean
}): IBCoreRequirementsArviointi => ({
  $class: 'fi.oph.koski.schema.IBCoreRequirementsArviointi',
  ...o
})

export const isIBCoreRequirementsArviointi = (
  a: any
): a is IBCoreRequirementsArviointi =>
  a?.$class === 'fi.oph.koski.schema.IBCoreRequirementsArviointi'
