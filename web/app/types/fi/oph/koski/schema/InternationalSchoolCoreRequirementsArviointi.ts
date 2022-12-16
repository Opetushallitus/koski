import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * InternationalSchoolCoreRequirementsArviointi
 *
 * @see `fi.oph.koski.schema.InternationalSchoolCoreRequirementsArviointi`
 */
export type InternationalSchoolCoreRequirementsArviointi = {
  $class: 'fi.oph.koski.schema.InternationalSchoolCoreRequirementsArviointi'
  predicted: boolean
  arvosana: Koodistokoodiviite<'arviointiasteikkocorerequirementsib', string>
  päivä?: string
  hyväksytty?: boolean
}

export const InternationalSchoolCoreRequirementsArviointi = (o: {
  predicted?: boolean
  arvosana: Koodistokoodiviite<'arviointiasteikkocorerequirementsib', string>
  päivä?: string
  hyväksytty?: boolean
}): InternationalSchoolCoreRequirementsArviointi => ({
  $class: 'fi.oph.koski.schema.InternationalSchoolCoreRequirementsArviointi',
  predicted: false,
  ...o
})

export const isInternationalSchoolCoreRequirementsArviointi = (
  a: any
): a is InternationalSchoolCoreRequirementsArviointi =>
  a?.$class === 'InternationalSchoolCoreRequirementsArviointi'
