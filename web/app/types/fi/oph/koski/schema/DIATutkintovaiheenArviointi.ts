import {
  DIAOppiaineenTutkintovaiheenNumeerinenArviointi,
  isDIAOppiaineenTutkintovaiheenNumeerinenArviointi
} from './DIAOppiaineenTutkintovaiheenNumeerinenArviointi'
import {
  DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi,
  isDIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi
} from './DIAOppiaineenTutkintovaiheenSuoritusmerkintaArviointi'

/**
 * DIATutkintovaiheenArviointi
 *
 * @see `fi.oph.koski.schema.DIATutkintovaiheenArviointi`
 */
export type DIATutkintovaiheenArviointi =
  | DIAOppiaineenTutkintovaiheenNumeerinenArviointi
  | DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi

export const isDIATutkintovaiheenArviointi = (
  a: any
): a is DIATutkintovaiheenArviointi =>
  isDIAOppiaineenTutkintovaiheenNumeerinenArviointi(a) ||
  isDIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi(a)
