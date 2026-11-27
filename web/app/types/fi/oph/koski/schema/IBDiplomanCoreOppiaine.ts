import {
  IBDiplomanExtendedEssay,
  isIBDiplomanExtendedEssay
} from './IBDiplomanExtendedEssay'
import {
  IBDiplomanTheoryOfKnowledge,
  isIBDiplomanTheoryOfKnowledge
} from './IBDiplomanTheoryOfKnowledge'

/**
 * IBDiplomanCoreOppiaine
 *
 * @see `fi.oph.koski.schema.IBDiplomanCoreOppiaine`
 */
export type IBDiplomanCoreOppiaine =
  IBDiplomanExtendedEssay | IBDiplomanTheoryOfKnowledge

export const isIBDiplomanCoreOppiaine = (a: any): a is IBDiplomanCoreOppiaine =>
  isIBDiplomanExtendedEssay(a) || isIBDiplomanTheoryOfKnowledge(a)
