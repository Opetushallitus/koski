import {
  MuuValtakunnallinenTutkinnonOsa,
  isMuuValtakunnallinenTutkinnonOsa
} from './MuuValtakunnallinenTutkinnonOsa'
import {
  PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa,
  isPaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa
} from './PaikallinenNayttotutkintoonValmistavanKoulutuksenOsa'
import {
  YhteinenTutkinnonOsa,
  isYhteinenTutkinnonOsa
} from './YhteinenTutkinnonOsa'

/**
 * NäyttötutkintoonValmistavanKoulutuksenOsa
 *
 * @see `fi.oph.koski.schema.NäyttötutkintoonValmistavanKoulutuksenOsa`
 */
export type NäyttötutkintoonValmistavanKoulutuksenOsa =
  | MuuValtakunnallinenTutkinnonOsa
  | PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa
  | YhteinenTutkinnonOsa

export const isNäyttötutkintoonValmistavanKoulutuksenOsa = (
  a: any
): a is NäyttötutkintoonValmistavanKoulutuksenOsa =>
  isMuuValtakunnallinenTutkinnonOsa(a) ||
  isPaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa(a) ||
  isYhteinenTutkinnonOsa(a)
