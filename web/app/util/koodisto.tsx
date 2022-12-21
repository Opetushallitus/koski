import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'

export const toKoodistokoodiviiteValue = (a: Koodistokoodiviite): string =>
  `${a.koodistoUri}_${a.koodiarvo}`

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
          .map((a) => `"${a}"`)
          .join(' | ')}>`
      )
    }
    return a as Koodistokoodiviite<U, A>
  }
  throw new Error(
    `Cannot cast Koodistokoodiviite<"${a.koodistoUri}"> to Koodistokoodiviite<"${koodistoUri}">`
  )
}

export const koodiviiteId = (koodiviite: Koodistokoodiviite): string =>
  `${koodiviite.koodistoUri}_${koodiviite.koodiarvo}`
