import {
  SuoritetutTutkinnotKoodistokoodiviite,
  isSuoritetutTutkinnotKoodistokoodiviite
} from './SuoritetutTutkinnotKoodistokoodiviite'

/**
 * SuoritetutTutkinnotOsaamisalajakso
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotOsaamisalajakso`
 */
export type SuoritetutTutkinnotOsaamisalajakso =
  | {
      $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotOsaamisalajakso'
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
