import * as A from 'fp-ts/Array'
import * as O from 'fp-ts/Option'
import * as Ord from 'fp-ts/Ord'
import { flow } from 'fp-ts/lib/function'
import * as number from 'fp-ts/number'
import { YleisenKielitutkinnonOsakokeenArviointi } from '../types/fi/oph/koski/schema/YleisenKielitutkinnonOsakokeenArviointi'

const YkiArvosanaOrder = ['11', '10', '9', '1', '2', '3', '4', '5', '6']

const YkiArviointiOrd = Ord.contramap(
  (a: YleisenKielitutkinnonOsakokeenArviointi) =>
    YkiArvosanaOrder.indexOf(a.arvosana.koodiarvo)
)(number.Ord)

export const ykiParasArvosana = flow(
  A.sort(YkiArviointiOrd),
  A.last,
  O.toNullable
)
