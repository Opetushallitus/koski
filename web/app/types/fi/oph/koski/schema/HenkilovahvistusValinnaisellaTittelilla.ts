import {
  HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla,
  isHenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla
} from './HenkilovahvistusValinnaisellaTittelillaJaValinnaisellaPaikkakunnalla'

/**
 * HenkilövahvistusValinnaisellaTittelillä
 *
 * @see `fi.oph.koski.schema.HenkilövahvistusValinnaisellaTittelillä`
 */
export type HenkilövahvistusValinnaisellaTittelillä =
  HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla

export const isHenkilövahvistusValinnaisellaTittelillä = (
  a: any
): a is HenkilövahvistusValinnaisellaTittelillä =>
  isHenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla(a)
