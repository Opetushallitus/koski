import {
  LukioonValmistavaÄidinkieliJaKirjallisuus,
  isLukioonValmistavaÄidinkieliJaKirjallisuus
} from './LukioonValmistavaAidinkieliJaKirjallisuus'
import {
  MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine,
  isMuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine
} from './MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine'
import { MuutKielet, isMuutKielet } from './MuutKielet'
import {
  PaikallinenLukioonValmistavanKoulutuksenOppiaine,
  isPaikallinenLukioonValmistavanKoulutuksenOppiaine
} from './PaikallinenLukioonValmistavanKoulutuksenOppiaine'

/**
 * LukioonValmistavanKoulutuksenOppiaine
 *
 * @see `fi.oph.koski.schema.LukioonValmistavanKoulutuksenOppiaine`
 */
export type LukioonValmistavanKoulutuksenOppiaine =
  | LukioonValmistavaÄidinkieliJaKirjallisuus
  | MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine
  | MuutKielet
  | PaikallinenLukioonValmistavanKoulutuksenOppiaine

export const isLukioonValmistavanKoulutuksenOppiaine = (
  a: any
): a is LukioonValmistavanKoulutuksenOppiaine =>
  isLukioonValmistavaÄidinkieliJaKirjallisuus(a) ||
  isMuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine(a) ||
  isMuutKielet(a) ||
  isPaikallinenLukioonValmistavanKoulutuksenOppiaine(a)
