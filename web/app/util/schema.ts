import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as O from 'fp-ts/Option'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import * as Ord from 'fp-ts/Ord'
import { Ord as StringOrd } from 'fp-ts/string'

export type OpiskeluoikeudenTilaLike = {
  opiskeluoikeusjaksot?: OpiskeluoikeudenJaksoLike[]
}

export type OpiskeluoikeudenJaksoLike = {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila' | 'virtaopiskeluoikeudentila',
    string
  >
}

export const OpiskeluoikeudenJaksoLikeOrd = Ord.contramap(
  (j: OpiskeluoikeudenJaksoLike) => j.alku
)(StringOrd)

export const viimeisinOpiskelujaksonTila = (
  tila: OpiskeluoikeudenTilaLike
): Koodistokoodiviite | undefined =>
  pipe(
    A.last(tila?.opiskeluoikeusjaksot || []),
    O.map((a) => a.tila),
    O.toUndefined
  )

export type ArviointiLike = {
  $class: string
  arvosana?: Koodistokoodiviite
}

export const viimeisinArviointi = (
  arviointi: ArviointiLike[]
): ArviointiLike | undefined => pipe(A.last(arviointi), O.toUndefined)
