import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi
 *
 * @see `fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi`
 */
export type DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi = {
  $class: 'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkodiatutkinto', 'S'>
  päivä?: string
  hyväksytty?: boolean
}

export const DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi = (
  o: {
    arvosana?: Koodistokoodiviite<'arviointiasteikkodiatutkinto', 'S'>
    päivä?: string
    hyväksytty?: boolean
  } = {}
): DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi => ({
  $class:
    'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi',
  arvosana: Koodistokoodiviite({
    koodiarvo: 'S',
    koodistoUri: 'arviointiasteikkodiatutkinto'
  }),
  ...o
})

DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi.className =
  'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi' as const

export const isDIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi = (
  a: any
): a is DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi =>
  a?.$class ===
  'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi'
