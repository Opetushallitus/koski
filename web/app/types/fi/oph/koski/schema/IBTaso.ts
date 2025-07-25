import {
  FitnessAndWellBeing,
  isFitnessAndWellBeing
} from './FitnessAndWellBeing'
import {
  IBDPCoreOppiaineLanguage,
  isIBDPCoreOppiaineLanguage
} from './IBDPCoreOppiaineLanguage'
import {
  IBDPCoreOppiaineMuu,
  isIBDPCoreOppiaineMuu
} from './IBDPCoreOppiaineMuu'
import { IBOppiaineLanguage, isIBOppiaineLanguage } from './IBOppiaineLanguage'
import { IBOppiaineMuu, isIBOppiaineMuu } from './IBOppiaineMuu'
import {
  InternationalSchoolMuuDiplomaOppiaine,
  isInternationalSchoolMuuDiplomaOppiaine
} from './InternationalSchoolMuuDiplomaOppiaine'
import {
  KieliDiplomaOppiaine,
  isKieliDiplomaOppiaine
} from './KieliDiplomaOppiaine'
import { MuuDiplomaOppiaine, isMuuDiplomaOppiaine } from './MuuDiplomaOppiaine'

/**
 * IBTaso
 *
 * @see `fi.oph.koski.schema.IBTaso`
 */
export type IBTaso =
  | FitnessAndWellBeing
  | IBDPCoreOppiaineLanguage
  | IBDPCoreOppiaineMuu
  | IBOppiaineLanguage
  | IBOppiaineMuu
  | InternationalSchoolMuuDiplomaOppiaine
  | KieliDiplomaOppiaine
  | MuuDiplomaOppiaine

export const isIBTaso = (a: any): a is IBTaso =>
  isFitnessAndWellBeing(a) ||
  isIBDPCoreOppiaineLanguage(a) ||
  isIBDPCoreOppiaineMuu(a) ||
  isIBOppiaineLanguage(a) ||
  isIBOppiaineMuu(a) ||
  isInternationalSchoolMuuDiplomaOppiaine(a) ||
  isKieliDiplomaOppiaine(a) ||
  isMuuDiplomaOppiaine(a)
