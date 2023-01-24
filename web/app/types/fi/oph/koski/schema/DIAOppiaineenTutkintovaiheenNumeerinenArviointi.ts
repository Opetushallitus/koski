import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * DIAOppiaineenTutkintovaiheenNumeerinenArviointi
 *
 * @see `fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenNumeerinenArviointi`
 */
export type DIAOppiaineenTutkintovaiheenNumeerinenArviointi = {
  $class: 'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenNumeerinenArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkodiatutkinto',
    | '0'
    | '1'
    | '2'
    | '2-'
    | '3'
    | '4'
    | '5'
    | '6'
    | '7'
    | '8'
    | '9'
    | '10'
    | '11'
    | '12'
    | '13'
    | '14'
    | '15'
  >
  päivä?: string
  lasketaanKokonaispistemäärään: boolean
  hyväksytty?: boolean
}

export const DIAOppiaineenTutkintovaiheenNumeerinenArviointi = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkodiatutkinto',
    | '0'
    | '1'
    | '2'
    | '2-'
    | '3'
    | '4'
    | '5'
    | '6'
    | '7'
    | '8'
    | '9'
    | '10'
    | '11'
    | '12'
    | '13'
    | '14'
    | '15'
  >
  päivä?: string
  lasketaanKokonaispistemäärään?: boolean
  hyväksytty?: boolean
}): DIAOppiaineenTutkintovaiheenNumeerinenArviointi => ({
  $class: 'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenNumeerinenArviointi',
  lasketaanKokonaispistemäärään: true,
  ...o
})

DIAOppiaineenTutkintovaiheenNumeerinenArviointi.className =
  'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenNumeerinenArviointi' as const

export const isDIAOppiaineenTutkintovaiheenNumeerinenArviointi = (
  a: any
): a is DIAOppiaineenTutkintovaiheenNumeerinenArviointi =>
  a?.$class ===
  'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenNumeerinenArviointi'
