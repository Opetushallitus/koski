import {
  OppisopimuksellinenOsaamisenHankkimistapa,
  isOppisopimuksellinenOsaamisenHankkimistapa
} from './OppisopimuksellinenOsaamisenHankkimistapa'
import {
  OsaamisenHankkimistapaIlmanLisätietoja,
  isOsaamisenHankkimistapaIlmanLisätietoja
} from './OsaamisenHankkimistapaIlmanLisatietoja'

/**
 * OsaamisenHankkimistapa
 *
 * @see `fi.oph.koski.schema.OsaamisenHankkimistapa`
 */
export type OsaamisenHankkimistapa =
  | OppisopimuksellinenOsaamisenHankkimistapa
  | OsaamisenHankkimistapaIlmanLisätietoja

export const isOsaamisenHankkimistapa = (a: any): a is OsaamisenHankkimistapa =>
  isOppisopimuksellinenOsaamisenHankkimistapa(a) ||
  isOsaamisenHankkimistapaIlmanLisätietoja(a)
