import {
  MuuValtakunnallinenTutkinnonOsa,
  isMuuValtakunnallinenTutkinnonOsa
} from './MuuValtakunnallinenTutkinnonOsa'
import {
  PaikallinenValmaKoulutuksenOsa,
  isPaikallinenValmaKoulutuksenOsa
} from './PaikallinenValmaKoulutuksenOsa'
import {
  YhteinenTutkinnonOsa,
  isYhteinenTutkinnonOsa
} from './YhteinenTutkinnonOsa'

/**
 * ValmaKoulutuksenOsa
 *
 * @see `fi.oph.koski.schema.ValmaKoulutuksenOsa`
 */
export type ValmaKoulutuksenOsa =
  | MuuValtakunnallinenTutkinnonOsa
  | PaikallinenValmaKoulutuksenOsa
  | YhteinenTutkinnonOsa

export const isValmaKoulutuksenOsa = (a: any): a is ValmaKoulutuksenOsa =>
  isMuuValtakunnallinenTutkinnonOsa(a) ||
  isPaikallinenValmaKoulutuksenOsa(a) ||
  isYhteinenTutkinnonOsa(a)
