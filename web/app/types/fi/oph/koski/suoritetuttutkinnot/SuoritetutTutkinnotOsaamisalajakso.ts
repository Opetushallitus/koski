import {
  SuoritetutTutkinnotKoodistokoodiviite,
  isSuoritetutTutkinnotKoodistokoodiviite
} from './SuoritetutTutkinnotKoodistokoodiviite'

/**
 * SuoritetutTutkinnotOsaamisalajakso
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotOsaamisalajakso`
 */
export type SuoritetutTutkinnotOsaamisalajakso =
  | {
      $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotOsaamisalajakso'
      osaamisala: SuoritetutTutkinnotKoodistokoodiviite
      alku?: string
      loppu?: string
    }
  | SuoritetutTutkinnotKoodistokoodiviite

export const isSuoritetutTutkinnotOsaamisalajakso = (
  a: any
): a is SuoritetutTutkinnotOsaamisalajakso =>
  isSuoritetutTutkinnotOsaamisalajakso(a) ||
  isSuoritetutTutkinnotKoodistokoodiviite(a)
