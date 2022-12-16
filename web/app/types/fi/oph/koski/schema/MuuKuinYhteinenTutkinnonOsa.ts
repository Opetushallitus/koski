import {
  MuuValtakunnallinenTutkinnonOsa,
  isMuuValtakunnallinenTutkinnonOsa
} from './MuuValtakunnallinenTutkinnonOsa'
import {
  PaikallinenTutkinnonOsa,
  isPaikallinenTutkinnonOsa
} from './PaikallinenTutkinnonOsa'

/**
 * MuuKuinYhteinenTutkinnonOsa
 *
 * @see `fi.oph.koski.schema.MuuKuinYhteinenTutkinnonOsa`
 */
export type MuuKuinYhteinenTutkinnonOsa =
  | MuuValtakunnallinenTutkinnonOsa
  | PaikallinenTutkinnonOsa

export const isMuuKuinYhteinenTutkinnonOsa = (
  a: any
): a is MuuKuinYhteinenTutkinnonOsa =>
  isMuuValtakunnallinenTutkinnonOsa(a) || isPaikallinenTutkinnonOsa(a)
