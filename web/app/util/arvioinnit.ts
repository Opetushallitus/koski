import * as A from 'fp-ts/Array'
import * as O from 'fp-ts/Option'
import * as Ord from 'fp-ts/Ord'
import * as number from 'fp-ts/number'
import * as string from 'fp-ts/string'
import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'
import { pipe } from 'fp-ts/lib/function'
import { ArviointiPäivämäärällä } from '../types/fi/oph/koski/schema/ArviointiPaivamaaralla'

type ConsolidatedArviointi = {
  päivä: string
  koodiarvo: string
  koodistoUri: string
}

const suoritusmerkinnät: Record<string, string> = {
  arviointiasteikkovst: 'Hyväksytty',
  arviointiasteikkotaiteenperusopetus: 'hyvaksytty',
  arviointiasteikkoyleissivistava: 'S'
}

const hylkäysmerkinnät: Record<string, string> = {
  arviointiasteikkovst: 'Hylätty',
  arviointiasteikkotaiteenperusopetus: 'hylatty',
  arviointiasteikkoyleissivistava: 'H'
}

const arvosanajärjestykset: Record<string, string[]> = {
  arviointiasteikkoib: ['O', 'F', 'S', '1', '2', '3', '4', '5', '6', '7']
}

const isSuoritusmerkintä = (arviointi: ConsolidatedArviointi): boolean =>
  suoritusmerkinnät[arviointi.koodistoUri] === arviointi.koodiarvo

const isHylkäysmerkintä = (arviointi: ConsolidatedArviointi): boolean =>
  hylkäysmerkinnät[arviointi.koodistoUri] === arviointi.koodiarvo

const isNumeerinenArvosana = (arviointi: ConsolidatedArviointi): boolean =>
  Number.isFinite(parseInt(arviointi.koodiarvo))

const contramapArviointi = Ord.contramap<ConsolidatedArviointi, Arviointi>(
  (arviointi) => ({
    päivä: (arviointi as any).päivä || '0000-00-00',
    koodiarvo: arviointi.arvosana.koodiarvo,
    koodistoUri: arviointi.arvosana.koodistoUri || ''
  })
)

const ConsolidatedArviointiOrd = Ord.fromCompare<ConsolidatedArviointi>(
  (first, second) => {
    if (first.koodiarvo === second.koodiarvo) {
      return 0
    }
    const order = arvosanajärjestykset[first.koodistoUri]
    if (order) {
      return number.Ord.compare(
        order.indexOf(first.koodiarvo),
        order.indexOf(second.koodiarvo)
      )
    }
    if (
      isSuoritusmerkintä(first) ||
      isSuoritusmerkintä(second) ||
      isHylkäysmerkintä(first) ||
      isHylkäysmerkintä(second)
    ) {
      return string.Ord.compare(first.päivä, second.päivä)
    }
    if (isNumeerinenArvosana(first) && isNumeerinenArvosana(first)) {
      return number.Ord.compare(
        parseFloat(first.koodiarvo),
        parseFloat(second.koodiarvo)
      )
    }
    return 0
  }
)

export const ArviointiOrd = contramapArviointi(ConsolidatedArviointiOrd)

export const parasArviointi = <T extends Arviointi>(
  arvioinnit?: T[]
): T | undefined =>
  pipe(arvioinnit || [], A.sort(ArviointiOrd), A.last, O.toUndefined)

export const parasArviointiIndex = <T extends Arviointi>(
  arvioinnit: T[]
): number | undefined =>
  pipe(
    O.fromNullable(parasArviointi(arvioinnit)),
    O.map((a) => arvioinnit.indexOf(a)),
    O.toUndefined
  )

export const viimeisinArviointi = <T extends Arviointi>(
  arvioinnit?: T[]
): T | undefined => pipe(arvioinnit || [], A.last, O.toUndefined)

export const ArviointipäiväOrd = Ord.contramap(
  (a: ArviointiPäivämäärällä) => a.päivä
)(string.Ord)
