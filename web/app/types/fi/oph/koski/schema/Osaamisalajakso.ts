import { Koodistokoodiviite, isKoodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString, isLocalizedString } from './LocalizedString'

/**
 * Osaamisalajakso
 *
 * @see `fi.oph.koski.schema.Osaamisalajakso`
 */
export type Osaamisalajakso =
  | {
      $class: 'fi.oph.koski.schema.Osaamisalajakso'
      osaamisala: Koodistokoodiviite<'osaamisala', string>
      alku?: string
      loppu?: string
    }
  | Koodistokoodiviite<'osaamisala', string>

export const isOsaamisalajakso = (a: any): a is Osaamisalajakso =>
  isOsaamisalajakso(a) || isKoodistokoodiviite(a)
