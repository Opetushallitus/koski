import {
  IBDPCoreOppiaineCAS,
  isIBDPCoreOppiaineCAS
} from './IBDPCoreOppiaineCAS'
import {
  IBDPCoreOppiaineExtendedEssay,
  isIBDPCoreOppiaineExtendedEssay
} from './IBDPCoreOppiaineExtendedEssay'
import {
  IBDPCoreOppiaineTheoryOfKnowledge,
  isIBDPCoreOppiaineTheoryOfKnowledge
} from './IBDPCoreOppiaineTheoryOfKnowledge'

/**
 * IBDPCoreOppiaine
 *
 * @see `fi.oph.koski.schema.IBDPCoreOppiaine`
 */
export type IBDPCoreOppiaine =
  | IBDPCoreOppiaineCAS
  | IBDPCoreOppiaineExtendedEssay
  | IBDPCoreOppiaineTheoryOfKnowledge

export const isIBDPCoreOppiaine = (a: any): a is IBDPCoreOppiaine =>
  isIBDPCoreOppiaineCAS(a) ||
  isIBDPCoreOppiaineExtendedEssay(a) ||
  isIBDPCoreOppiaineTheoryOfKnowledge(a)
