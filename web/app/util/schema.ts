import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as O from 'fp-ts/Option'
import * as Ord from 'fp-ts/Ord'
import { Ord as StringOrd } from 'fp-ts/string'
import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'
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
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { OpiskeluoikeudenTila } from '../types/fi/oph/koski/schema/OpiskeluoikeudenTila'
import { Opiskeluoikeusjakso } from '../types/fi/oph/koski/schema/Opiskeluoikeusjakso'
import { Vahvistus } from '../types/fi/oph/koski/schema/Vahvistus'
import { ItemOf } from './types'

export type OpiskeluoikeusjaksotOf<T extends OpiskeluoikeudenTila> =
  T['opiskeluoikeusjaksot']

export type OpiskeluoikeusjaksoOf<T extends OpiskeluoikeudenTila> = ItemOf<
  T['opiskeluoikeusjaksot']
>

export const OpiskeluoikeusjaksoOrd = Ord.contramap(
  (j: Opiskeluoikeusjakso) => j.alku
)(StringOrd)

export const viimeisinOpiskelujakso = <T extends OpiskeluoikeudenTila>(
  tila: T
): ItemOf<T['opiskeluoikeusjaksot']> | undefined => {
  const jaksot = tila?.opiskeluoikeusjaksot || []
  return jaksot[jaksot.length - 1]
}

export const viimeisinOpiskelujaksonTila = (
  tila: OpiskeluoikeudenTila
): Koodistokoodiviite | undefined =>
  pipe(
    O.fromNullable(viimeisinOpiskelujakso(tila)),
    O.map((a) => a.tila),
    O.toUndefined
  )

export const viimeisinArviointi = (
  arviointi: Arviointi[]
): Arviointi | undefined => pipe(A.last(arviointi), O.toUndefined)

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
