import { IBOppiaineCAS, isIBOppiaineCAS } from './IBOppiaineCAS'
import {
  IBOppiaineExtendedEssay,
  isIBOppiaineExtendedEssay
} from './IBOppiaineExtendedEssay'
import {
  IBOppiaineTheoryOfKnowledge,
  isIBOppiaineTheoryOfKnowledge
} from './IBOppiaineTheoryOfKnowledge'

/**
 * IBDPCoreOppiaine
 *
 * @see `fi.oph.koski.schema.IBDPCoreOppiaine`
 */
export type IBDPCoreOppiaine =
  | IBOppiaineCAS
  | IBOppiaineExtendedEssay
  | IBOppiaineTheoryOfKnowledge

export const isIBDPCoreOppiaine = (a: any): a is IBDPCoreOppiaine =>
  isIBOppiaineCAS(a) ||
  isIBOppiaineExtendedEssay(a) ||
  isIBOppiaineTheoryOfKnowledge(a)
