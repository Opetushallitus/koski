import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * SanallinenInternationalSchoolOppiaineenArviointi
 *
 * @see `fi.oph.koski.schema.SanallinenInternationalSchoolOppiaineenArviointi`
 */
export type SanallinenInternationalSchoolOppiaineenArviointi = {
  $class: 'fi.oph.koski.schema.SanallinenInternationalSchoolOppiaineenArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkointernationalschool', string>
  päivä?: string
  hyväksytty?: boolean
}

export const SanallinenInternationalSchoolOppiaineenArviointi = (o: {
  arvosana: Koodistokoodiviite<'arviointiasteikkointernationalschool', string>
  päivä?: string
  hyväksytty?: boolean
}): SanallinenInternationalSchoolOppiaineenArviointi => ({
  $class:
    'fi.oph.koski.schema.SanallinenInternationalSchoolOppiaineenArviointi',
  ...o
})

export const isSanallinenInternationalSchoolOppiaineenArviointi = (
  a: any
): a is SanallinenInternationalSchoolOppiaineenArviointi =>
  a?.$class === 'SanallinenInternationalSchoolOppiaineenArviointi'
