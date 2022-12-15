import {
  DiplomaCoreRequirementsOppiaineenSuoritus,
  isDiplomaCoreRequirementsOppiaineenSuoritus
} from './DiplomaCoreRequirementsOppiaineenSuoritus'
import {
  DiplomaOppiaineenSuoritus,
  isDiplomaOppiaineenSuoritus
} from './DiplomaOppiaineenSuoritus'

/**
 * DiplomaIBOppiaineenSuoritus
 *
 * @see `fi.oph.koski.schema.DiplomaIBOppiaineenSuoritus`
 */
export type DiplomaIBOppiaineenSuoritus =
  | DiplomaCoreRequirementsOppiaineenSuoritus
  | DiplomaOppiaineenSuoritus

export const isDiplomaIBOppiaineenSuoritus = (
  a: any
): a is DiplomaIBOppiaineenSuoritus =>
  isDiplomaCoreRequirementsOppiaineenSuoritus(a) ||
  isDiplomaOppiaineenSuoritus(a)
