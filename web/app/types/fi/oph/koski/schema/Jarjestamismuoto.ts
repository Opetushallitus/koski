import {
  JärjestämismuotoIlmanLisätietoja,
  isJärjestämismuotoIlmanLisätietoja
} from './JarjestamismuotoIlmanLisatietoja'
import {
  OppisopimuksellinenJärjestämismuoto,
  isOppisopimuksellinenJärjestämismuoto
} from './OppisopimuksellinenJarjestamismuoto'

/**
 * Järjestämismuoto
 *
 * @see `fi.oph.koski.schema.Järjestämismuoto`
 */
export type Järjestämismuoto =
  | JärjestämismuotoIlmanLisätietoja
  | OppisopimuksellinenJärjestämismuoto

export const isJärjestämismuoto = (a: any): a is Järjestämismuoto =>
  isJärjestämismuotoIlmanLisätietoja(a) ||
  isOppisopimuksellinenJärjestämismuoto(a)
