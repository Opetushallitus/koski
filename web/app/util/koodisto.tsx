import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { KoodiViite } from '../types/fi/oph/koski/schema/KoodiViite'
import {
  isLukionOppiaineidenOppimäärätKoodi2019,
  LukionOppiaineidenOppimäärätKoodi2019
} from '../types/fi/oph/koski/schema/LukionOppiaineidenOppimaaratKoodi2019'
import {
  isPaikallinenKoodi,
  PaikallinenKoodi
} from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import {
  isSynteettinenKoodiviite,
  SynteettinenKoodiviite
} from '../types/fi/oph/koski/schema/SynteettinenKoodiviite'

export type KoodistoUriOf<T extends Koodistokoodiviite> = T['koodistoUri']
export type KoodiarvotOf<T extends Koodistokoodiviite> = T['koodiarvo']

export const koodiviiteId = (a: KoodiViite): string =>
  isKoodiviiteUriOptional(a) ? a.koodiarvo : koodistokoodiviiteId(a)

export const koodistokoodiviiteId = (a: Koodistokoodiviite): string =>
  `${a.koodistoUri}_${a.koodiarvo}`

export type KoodiviiteWithOptionalUri =
  | PaikallinenKoodi
  | LukionOppiaineidenOppimäärätKoodi2019
  | SynteettinenKoodiviite

export const isKoodiviiteUriOptional = (
  a: KoodiViite
): a is KoodiviiteWithOptionalUri =>
  isPaikallinenKoodi(a) ||
  isLukionOppiaineidenOppimäärätKoodi2019(a) ||
  isSynteettinenKoodiviite(a)

export const asKoodiviite = <U extends string, A extends string = string>(
  a: Koodistokoodiviite,
  koodistoUri: U,
  koodiarvot?: A[]
): Koodistokoodiviite<U, A> => {
  if (koodistoUri === a.koodistoUri) {
    if (koodiarvot && !koodiarvot.includes(a.koodiarvo as A)) {
      throw new Error(
        `Cannot cast Koodistokoodiviite<"${a.koodistoUri}", "${
          a.koodiarvo
        }"> to Koodistokoodiviite<"${koodistoUri}", ${koodiarvot
          .map((str) => `"${str}"`)
          .join(' | ')}>`
      )
    }
    return a as Koodistokoodiviite<U, A>
  }
  throw new Error(
    `Cannot cast Koodistokoodiviite<"${a.koodistoUri}"> to Koodistokoodiviite<"${koodistoUri}">`
  )
}