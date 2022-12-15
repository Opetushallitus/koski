import {
  EuropeanSchoolOfHelsinkiKielioppiaine,
  isEuropeanSchoolOfHelsinkiKielioppiaine
} from './EuropeanSchoolOfHelsinkiKielioppiaine'
import {
  EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek,
  isEuropeanSchoolOfHelsinkiKielioppiaineAncientGreek
} from './EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek'
import {
  EuropeanSchoolOfHelsinkiKielioppiaineLatin,
  isEuropeanSchoolOfHelsinkiKielioppiaineLatin
} from './EuropeanSchoolOfHelsinkiKielioppiaineLatin'
import {
  EuropeanSchoolOfHelsinkiMuuOppiaine,
  isEuropeanSchoolOfHelsinkiMuuOppiaine
} from './EuropeanSchoolOfHelsinkiMuuOppiaine'

/**
 * PrimarySuorituskielenVaativaOppimisalue
 *
 * @see `fi.oph.koski.schema.PrimarySuorituskielenVaativaOppimisalue`
 */
export type PrimarySuorituskielenVaativaOppimisalue =
  | EuropeanSchoolOfHelsinkiKielioppiaine
  | EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek
  | EuropeanSchoolOfHelsinkiKielioppiaineLatin
  | EuropeanSchoolOfHelsinkiMuuOppiaine

export const isPrimarySuorituskielenVaativaOppimisalue = (
  a: any
): a is PrimarySuorituskielenVaativaOppimisalue =>
  isEuropeanSchoolOfHelsinkiKielioppiaine(a) ||
  isEuropeanSchoolOfHelsinkiKielioppiaineAncientGreek(a) ||
  isEuropeanSchoolOfHelsinkiKielioppiaineLatin(a) ||
  isEuropeanSchoolOfHelsinkiMuuOppiaine(a)
