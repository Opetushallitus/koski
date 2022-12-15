import {
  PaikallinenLukioonValmistavanKoulutuksenKurssi,
  isPaikallinenLukioonValmistavanKoulutuksenKurssi
} from './PaikallinenLukioonValmistavanKoulutuksenKurssi'
import {
  ValtakunnallinenLukioonValmistavanKoulutuksenKurssi,
  isValtakunnallinenLukioonValmistavanKoulutuksenKurssi
} from './ValtakunnallinenLukioonValmistavanKoulutuksenKurssi'

/**
 * LukioonValmistavanKoulutuksenKurssi
 *
 * @see `fi.oph.koski.schema.LukioonValmistavanKoulutuksenKurssi`
 */
export type LukioonValmistavanKoulutuksenKurssi =
  | PaikallinenLukioonValmistavanKoulutuksenKurssi
  | ValtakunnallinenLukioonValmistavanKoulutuksenKurssi

export const isLukioonValmistavanKoulutuksenKurssi = (
  a: any
): a is LukioonValmistavanKoulutuksenKurssi =>
  isPaikallinenLukioonValmistavanKoulutuksenKurssi(a) ||
  isValtakunnallinenLukioonValmistavanKoulutuksenKurssi(a)
