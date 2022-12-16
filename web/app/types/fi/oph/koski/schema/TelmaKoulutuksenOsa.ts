import {
  MuuValtakunnallinenTutkinnonOsa,
  isMuuValtakunnallinenTutkinnonOsa
} from './MuuValtakunnallinenTutkinnonOsa'
import {
  PaikallinenTelmaKoulutuksenOsa,
  isPaikallinenTelmaKoulutuksenOsa
} from './PaikallinenTelmaKoulutuksenOsa'
import {
  YhteinenTutkinnonOsa,
  isYhteinenTutkinnonOsa
} from './YhteinenTutkinnonOsa'

/**
 * TelmaKoulutuksenOsa
 *
 * @see `fi.oph.koski.schema.TelmaKoulutuksenOsa`
 */
export type TelmaKoulutuksenOsa =
  | MuuValtakunnallinenTutkinnonOsa
  | PaikallinenTelmaKoulutuksenOsa
  | YhteinenTutkinnonOsa

export const isTelmaKoulutuksenOsa = (a: any): a is TelmaKoulutuksenOsa =>
  isMuuValtakunnallinenTutkinnonOsa(a) ||
  isPaikallinenTelmaKoulutuksenOsa(a) ||
  isYhteinenTutkinnonOsa(a)
