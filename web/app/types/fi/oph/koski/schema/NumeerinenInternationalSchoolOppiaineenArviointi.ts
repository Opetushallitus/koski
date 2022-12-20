import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * NumeerinenInternationalSchoolOppiaineenArviointi
 *
 * @see `fi.oph.koski.schema.NumeerinenInternationalSchoolOppiaineenArviointi`
 */
export type NumeerinenInternationalSchoolOppiaineenArviointi = {
  $class: 'fi.oph.koski.schema.NumeerinenInternationalSchoolOppiaineenArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoib',
    'S' | 'F' | '1' | '2' | '3' | '4' | '5' | '6' | '7'
  >
  päivä?: string
  hyväksytty?: boolean
}

export const NumeerinenInternationalSchoolOppiaineenArviointi = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoib',
    'S' | 'F' | '1' | '2' | '3' | '4' | '5' | '6' | '7'
  >
  päivä?: string
  hyväksytty?: boolean
}): NumeerinenInternationalSchoolOppiaineenArviointi => ({
  $class:
    'fi.oph.koski.schema.NumeerinenInternationalSchoolOppiaineenArviointi',
  ...o
})

export const isNumeerinenInternationalSchoolOppiaineenArviointi = (
  a: any
): a is NumeerinenInternationalSchoolOppiaineenArviointi =>
  a?.$class ===
  'fi.oph.koski.schema.NumeerinenInternationalSchoolOppiaineenArviointi'
