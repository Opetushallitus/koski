import { Koodistokoodiviite, isKoodistokoodiviite } from './Koodistokoodiviite'
import {
  LukionOppiaineidenOppimäärätKoodi2019,
  isLukionOppiaineidenOppimäärätKoodi2019
} from './LukionOppiaineidenOppimaaratKoodi2019'
import { PaikallinenKoodi, isPaikallinenKoodi } from './PaikallinenKoodi'
import {
  SynteettinenKoodiviite,
  isSynteettinenKoodiviite
} from './SynteettinenKoodiviite'

/**
 * KoodiViite
 *
 * @see `fi.oph.koski.schema.KoodiViite`
 */
export type KoodiViite =
  | Koodistokoodiviite
  | LukionOppiaineidenOppimäärätKoodi2019
  | PaikallinenKoodi
  | SynteettinenKoodiviite

export const isKoodiViite = (a: any): a is KoodiViite =>
  isKoodistokoodiviite(a) ||
  isLukionOppiaineidenOppimäärätKoodi2019(a) ||
  isPaikallinenKoodi(a) ||
  isSynteettinenKoodiviite(a)
