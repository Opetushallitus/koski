import {
  AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  isAktiivisetJaPäättyneetOpinnotKoodistokoodiviite
} from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotOsaamisalajakso
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOsaamisalajakso`
 */
export type AktiivisetJaPäättyneetOpinnotOsaamisalajakso =
  | {
      $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOsaamisalajakso'
      osaamisala: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
      alku?: string
      loppu?: string
    }
  | AktiivisetJaPäättyneetOpinnotKoodistokoodiviite

export const isAktiivisetJaPäättyneetOpinnotOsaamisalajakso = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotOsaamisalajakso =>
  isAktiivisetJaPäättyneetOpinnotOsaamisalajakso(a) ||
  isAktiivisetJaPäättyneetOpinnotKoodistokoodiviite(a)
