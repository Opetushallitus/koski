import {
  FitnessAndWellBeing,
  isFitnessAndWellBeing
} from './FitnessAndWellBeing'
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
 * InternationalSchoolIBOppiaine
 *
 * @see `fi.oph.koski.schema.InternationalSchoolIBOppiaine`
 */
export type InternationalSchoolIBOppiaine =
  | FitnessAndWellBeing
  | InternationalSchoolMuuDiplomaOppiaine
  | KieliDiplomaOppiaine
  | MuuDiplomaOppiaine

export const isInternationalSchoolIBOppiaine = (
  a: any
): a is InternationalSchoolIBOppiaine =>
  isFitnessAndWellBeing(a) ||
  isInternationalSchoolMuuDiplomaOppiaine(a) ||
  isKieliDiplomaOppiaine(a) ||
  isMuuDiplomaOppiaine(a)
