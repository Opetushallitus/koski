import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * InternationalSchoolIBOppiaineenArviointi
 *
 * @see `fi.oph.koski.schema.InternationalSchoolIBOppiaineenArviointi`
 */
export type InternationalSchoolIBOppiaineenArviointi = {
  $class: 'fi.oph.koski.schema.InternationalSchoolIBOppiaineenArviointi'
  predicted: boolean
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoib',
    'S' | 'F' | '1' | '2' | '3' | '4' | '5' | '6' | '7'
  >
  päivä?: string
  hyväksytty?: boolean
}

export const InternationalSchoolIBOppiaineenArviointi = (o: {
  predicted?: boolean
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoib',
    'S' | 'F' | '1' | '2' | '3' | '4' | '5' | '6' | '7'
  >
  päivä?: string
  hyväksytty?: boolean
}): InternationalSchoolIBOppiaineenArviointi => ({
  $class: 'fi.oph.koski.schema.InternationalSchoolIBOppiaineenArviointi',
  predicted: false,
  ...o
})

export const isInternationalSchoolIBOppiaineenArviointi = (
  a: any
): a is InternationalSchoolIBOppiaineenArviointi =>
  a?.$class === 'InternationalSchoolIBOppiaineenArviointi'
