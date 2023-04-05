import { Koodistokoodiviite, isKoodistokoodiviite } from './Koodistokoodiviite'
import {
  KorkeakoulunPaikallinenArvosana,
  isKorkeakoulunPaikallinenArvosana
} from './KorkeakoulunPaikallinenArvosana'
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
  | KorkeakoulunPaikallinenArvosana
  | LukionOppiaineidenOppimäärätKoodi2019
  | PaikallinenKoodi
  | SynteettinenKoodiviite

export const isKoodiViite = (a: any): a is KoodiViite =>
  isKoodistokoodiviite(a) ||
  isKorkeakoulunPaikallinenArvosana(a) ||
  isLukionOppiaineidenOppimäärätKoodi2019(a) ||
  isPaikallinenKoodi(a) ||
  isSynteettinenKoodiviite(a)
