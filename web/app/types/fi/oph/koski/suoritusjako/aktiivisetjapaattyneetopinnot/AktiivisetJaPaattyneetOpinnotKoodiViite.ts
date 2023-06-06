import {
  AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  isAktiivisetJaPäättyneetOpinnotKoodistokoodiviite
} from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import {
  AktiivisetJaPäättyneetOpinnotPaikallinenKoodi,
  isAktiivisetJaPäättyneetOpinnotPaikallinenKoodi
} from './AktiivisetJaPaattyneetOpinnotPaikallinenKoodi'

/**
 * AktiivisetJaPäättyneetOpinnotKoodiViite
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKoodiViite`
 */
export type AktiivisetJaPäättyneetOpinnotKoodiViite =
  | AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  | AktiivisetJaPäättyneetOpinnotPaikallinenKoodi

export const isAktiivisetJaPäättyneetOpinnotKoodiViite = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotKoodiViite =>
  isAktiivisetJaPäättyneetOpinnotKoodistokoodiviite(a) ||
  isAktiivisetJaPäättyneetOpinnotPaikallinenKoodi(a)
