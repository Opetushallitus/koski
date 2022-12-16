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
 * SecondaryOppiaine
 *
 * @see `fi.oph.koski.schema.SecondaryOppiaine`
 */
export type SecondaryOppiaine =
  | EuropeanSchoolOfHelsinkiKielioppiaine
  | EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek
  | EuropeanSchoolOfHelsinkiKielioppiaineLatin
  | EuropeanSchoolOfHelsinkiMuuOppiaine

export const isSecondaryOppiaine = (a: any): a is SecondaryOppiaine =>
  isEuropeanSchoolOfHelsinkiKielioppiaine(a) ||
  isEuropeanSchoolOfHelsinkiKielioppiaineAncientGreek(a) ||
  isEuropeanSchoolOfHelsinkiKielioppiaineLatin(a) ||
  isEuropeanSchoolOfHelsinkiMuuOppiaine(a)
