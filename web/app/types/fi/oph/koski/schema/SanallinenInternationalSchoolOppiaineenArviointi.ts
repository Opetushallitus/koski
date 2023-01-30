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

SanallinenInternationalSchoolOppiaineenArviointi.className =
  'fi.oph.koski.schema.SanallinenInternationalSchoolOppiaineenArviointi' as const

export const isSanallinenInternationalSchoolOppiaineenArviointi = (
  a: any
): a is SanallinenInternationalSchoolOppiaineenArviointi =>
  a?.$class ===
  'fi.oph.koski.schema.SanallinenInternationalSchoolOppiaineenArviointi'
