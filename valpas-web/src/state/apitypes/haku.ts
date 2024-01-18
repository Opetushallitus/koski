import * as A from "fp-ts/Array"
import { pipe } from "fp-ts/lib/function"
import * as O from "fp-ts/Option"
import * as Ord from "fp-ts/Ord"
import * as string from "fp-ts/string"
import { ISODate, ISODateTime, LocalizedString, Oid } from "../common"
import { Hakutoive, SuppeaHakutoive } from "./hakutoive"

// Tyypit

export type HakuLaajatTiedot = {
  hakuOid: Oid
  hakuNimi?: LocalizedString
  hakemusOid: Oid
  hakemusUrl: string
  aktiivinenHaku: boolean
  hakuAlkaa: ISODateTime
  muokattu?: ISODateTime
  hakutoiveet: Hakutoive[]
}

export type HakuSuppeatTiedot = Pick<
  HakuLaajatTiedot,
  | "hakuOid"
  | "hakuNimi"
  | "hakemusOid"
  | "hakemusUrl"
  | "aktiivinenHaku"
  | "muokattu"
> & {
  hakutoiveet: SuppeaHakutoive[]
}

export const hakuMuokattuOrd = Ord.contramap(
  (h: HakuSuppeatTiedot) => (h["muokattu"] as ISODate) || "0000-00-00",
)(Ord.reverse(string.Ord))

export const latestHaku = (haut: HakuSuppeatTiedot[]) =>
  pipe(haut, A.sortBy([hakuMuokattuOrd]), A.head, O.toNullable)

export const sortHakuLaajatTiedot = A.sortBy<HakuLaajatTiedot>([
  hakuMuokattuOrd,
])

export const selectByHakutoive = (
  haut: HakuSuppeatTiedot[],
  predicate: (hakutoive: SuppeaHakutoive) => boolean,
) =>
  A.chain((haku: HakuSuppeatTiedot) => haku.hakutoiveet.filter(predicate))(haut)
