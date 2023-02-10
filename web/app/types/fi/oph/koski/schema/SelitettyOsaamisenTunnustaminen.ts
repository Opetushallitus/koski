import {
  OsaamisenTunnustaminen,
  isOsaamisenTunnustaminen
} from './OsaamisenTunnustaminen'
import {
  TaiteenPerusopetuksenOsasuorituksenTunnustus,
  isTaiteenPerusopetuksenOsasuorituksenTunnustus
} from './TaiteenPerusopetuksenOsasuorituksenTunnustus'

/**
 * SelitettyOsaamisenTunnustaminen
 *
 * @see `fi.oph.koski.schema.SelitettyOsaamisenTunnustaminen`
 */
export type SelitettyOsaamisenTunnustaminen =
  | OsaamisenTunnustaminen
  | TaiteenPerusopetuksenOsasuorituksenTunnustus

export const isSelitettyOsaamisenTunnustaminen = (
  a: any
): a is SelitettyOsaamisenTunnustaminen =>
  isOsaamisenTunnustaminen(a) ||
  isTaiteenPerusopetuksenOsasuorituksenTunnustus(a)
