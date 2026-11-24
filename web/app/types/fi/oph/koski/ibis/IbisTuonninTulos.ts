import { IbisParseError } from './IbisParseError'
import { IbisTuonninRivi } from './IbisTuonninRivi'

/**
 * IbisTuonninTulos
 *
 * @see `fi.oph.koski.ibis.IbisTuonninTulos`
 */
export type IbisTuonninTulos = {
  $class: 'fi.oph.koski.ibis.IbisTuonninTulos'
  tallennettu: boolean
  jäsennysvirheet: Array<IbisParseError>
  rivit: Array<IbisTuonninRivi>
}

export const IbisTuonninTulos = (o: {
  tallennettu: boolean
  jäsennysvirheet?: Array<IbisParseError>
  rivit?: Array<IbisTuonninRivi>
}): IbisTuonninTulos => ({
  $class: 'fi.oph.koski.ibis.IbisTuonninTulos',
  jäsennysvirheet: [],
  rivit: [],
  ...o
})

IbisTuonninTulos.className = 'fi.oph.koski.ibis.IbisTuonninTulos' as const

export const isIbisTuonninTulos = (a: any): a is IbisTuonninTulos =>
  a?.$class === 'fi.oph.koski.ibis.IbisTuonninTulos'
