import { HenkilötiedotJaOid, isHenkilötiedotJaOid } from './HenkilotiedotJaOid'
import { OidHenkilö, isOidHenkilö } from './OidHenkilo'
import {
  TäydellisetHenkilötiedot,
  isTäydellisetHenkilötiedot
} from './TaydellisetHenkilotiedot'
import { UusiHenkilö, isUusiHenkilö } from './UusiHenkilo'

/**
 * Henkilö
 *
 * @see `fi.oph.koski.schema.Henkilö`
 */
export type Henkilö =
  | HenkilötiedotJaOid
  | OidHenkilö
  | TäydellisetHenkilötiedot
  | UusiHenkilö

export const isHenkilö = (a: any): a is Henkilö =>
  isHenkilötiedotJaOid(a) ||
  isOidHenkilö(a) ||
  isTäydellisetHenkilötiedot(a) ||
  isUusiHenkilö(a)
