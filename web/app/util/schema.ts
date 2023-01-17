import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as O from 'fp-ts/Option'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import * as Ord from 'fp-ts/Ord'
import { Ord as StringOrd } from 'fp-ts/string'
import { Organisaatio } from '../types/fi/oph/koski/schema/Organisaatio'
import { OrganisaatiohenkilöValinnaisellaTittelillä } from '../types/fi/oph/koski/schema/OrganisaatiohenkiloValinnaisellaTittelilla'
import { Vahvistus } from '../types/fi/oph/koski/schema/Vahvistus'
import {
  HenkilövahvistusPaikkakunnalla,
  isHenkilövahvistusPaikkakunnalla
} from '../types/fi/oph/koski/schema/HenkilovahvistusPaikkakunnalla'
import {
  HenkilövahvistusValinnaisellaPaikkakunnalla,
  isHenkilövahvistusValinnaisellaPaikkakunnalla
} from '../types/fi/oph/koski/schema/HenkilovahvistusValinnaisellaPaikkakunnalla'
import {
  HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla,
  isHenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla
} from '../types/fi/oph/koski/schema/HenkilovahvistusValinnaisellaTittelillaJaValinnaisellaPaikkakunnalla'

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

export const viimeisinOpiskelujakso = (
  tila: OpiskeluoikeudenTilaLike
): OpiskeluoikeudenJaksoLike | undefined =>
  pipe(A.last(tila?.opiskeluoikeusjaksot || []), O.toUndefined)

export const viimeisinOpiskelujaksonTila = (
  tila: OpiskeluoikeudenTilaLike
): Koodistokoodiviite | undefined =>
  pipe(
    O.fromNullable(viimeisinOpiskelujakso(tila)),
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

export const isHenkilövahvistus = (
  vahvistus: Vahvistus
): vahvistus is
  | HenkilövahvistusPaikkakunnalla
  | HenkilövahvistusValinnaisellaPaikkakunnalla
  | HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla =>
  isHenkilövahvistusPaikkakunnalla(vahvistus) ||
  isHenkilövahvistusValinnaisellaPaikkakunnalla(vahvistus) ||
  isHenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla(
    vahvistus
  )
