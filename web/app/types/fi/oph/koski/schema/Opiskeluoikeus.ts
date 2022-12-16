import {
  AikuistenPerusopetuksenOpiskeluoikeus,
  isAikuistenPerusopetuksenOpiskeluoikeus
} from './AikuistenPerusopetuksenOpiskeluoikeus'
import {
  AmmatillinenOpiskeluoikeus,
  isAmmatillinenOpiskeluoikeus
} from './AmmatillinenOpiskeluoikeus'
import { DIAOpiskeluoikeus, isDIAOpiskeluoikeus } from './DIAOpiskeluoikeus'
import {
  EsiopetuksenOpiskeluoikeus,
  isEsiopetuksenOpiskeluoikeus
} from './EsiopetuksenOpiskeluoikeus'
import {
  EuropeanSchoolOfHelsinkiOpiskeluoikeus,
  isEuropeanSchoolOfHelsinkiOpiskeluoikeus
} from './EuropeanSchoolOfHelsinkiOpiskeluoikeus'
import { IBOpiskeluoikeus, isIBOpiskeluoikeus } from './IBOpiskeluoikeus'
import {
  InternationalSchoolOpiskeluoikeus,
  isInternationalSchoolOpiskeluoikeus
} from './InternationalSchoolOpiskeluoikeus'
import {
  KorkeakoulunOpiskeluoikeus,
  isKorkeakoulunOpiskeluoikeus
} from './KorkeakoulunOpiskeluoikeus'
import {
  LukionOpiskeluoikeus,
  isLukionOpiskeluoikeus
} from './LukionOpiskeluoikeus'
import {
  LukioonValmistavanKoulutuksenOpiskeluoikeus,
  isLukioonValmistavanKoulutuksenOpiskeluoikeus
} from './LukioonValmistavanKoulutuksenOpiskeluoikeus'
import {
  MuunKuinSäännellynKoulutuksenOpiskeluoikeus,
  isMuunKuinSäännellynKoulutuksenOpiskeluoikeus
} from './MuunKuinSaannellynKoulutuksenOpiskeluoikeus'
import {
  PerusopetukseenValmistavanOpetuksenOpiskeluoikeus,
  isPerusopetukseenValmistavanOpetuksenOpiskeluoikeus
} from './PerusopetukseenValmistavanOpetuksenOpiskeluoikeus'
import {
  PerusopetuksenLisäopetuksenOpiskeluoikeus,
  isPerusopetuksenLisäopetuksenOpiskeluoikeus
} from './PerusopetuksenLisaopetuksenOpiskeluoikeus'
import {
  PerusopetuksenOpiskeluoikeus,
  isPerusopetuksenOpiskeluoikeus
} from './PerusopetuksenOpiskeluoikeus'
import {
  TaiteenPerusopetuksenOpiskeluoikeus,
  isTaiteenPerusopetuksenOpiskeluoikeus
} from './TaiteenPerusopetuksenOpiskeluoikeus'
import {
  TutkintokoulutukseenValmentavanOpiskeluoikeus,
  isTutkintokoulutukseenValmentavanOpiskeluoikeus
} from './TutkintokoulutukseenValmentavanOpiskeluoikeus'
import {
  VapaanSivistystyönOpiskeluoikeus,
  isVapaanSivistystyönOpiskeluoikeus
} from './VapaanSivistystyonOpiskeluoikeus'
import {
  YlioppilastutkinnonOpiskeluoikeus,
  isYlioppilastutkinnonOpiskeluoikeus
} from './YlioppilastutkinnonOpiskeluoikeus'

/**
 * Opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.Opiskeluoikeus`
 */
export type Opiskeluoikeus =
  | AikuistenPerusopetuksenOpiskeluoikeus
  | AmmatillinenOpiskeluoikeus
  | DIAOpiskeluoikeus
  | EsiopetuksenOpiskeluoikeus
  | EuropeanSchoolOfHelsinkiOpiskeluoikeus
  | IBOpiskeluoikeus
  | InternationalSchoolOpiskeluoikeus
  | KorkeakoulunOpiskeluoikeus
  | LukionOpiskeluoikeus
  | LukioonValmistavanKoulutuksenOpiskeluoikeus
  | MuunKuinSäännellynKoulutuksenOpiskeluoikeus
  | PerusopetukseenValmistavanOpetuksenOpiskeluoikeus
  | PerusopetuksenLisäopetuksenOpiskeluoikeus
  | PerusopetuksenOpiskeluoikeus
  | TaiteenPerusopetuksenOpiskeluoikeus
  | TutkintokoulutukseenValmentavanOpiskeluoikeus
  | VapaanSivistystyönOpiskeluoikeus
  | YlioppilastutkinnonOpiskeluoikeus

export const isOpiskeluoikeus = (a: any): a is Opiskeluoikeus =>
  isAikuistenPerusopetuksenOpiskeluoikeus(a) ||
  isAmmatillinenOpiskeluoikeus(a) ||
  isDIAOpiskeluoikeus(a) ||
  isEsiopetuksenOpiskeluoikeus(a) ||
  isEuropeanSchoolOfHelsinkiOpiskeluoikeus(a) ||
  isIBOpiskeluoikeus(a) ||
  isInternationalSchoolOpiskeluoikeus(a) ||
  isKorkeakoulunOpiskeluoikeus(a) ||
  isLukionOpiskeluoikeus(a) ||
  isLukioonValmistavanKoulutuksenOpiskeluoikeus(a) ||
  isMuunKuinSäännellynKoulutuksenOpiskeluoikeus(a) ||
  isPerusopetukseenValmistavanOpetuksenOpiskeluoikeus(a) ||
  isPerusopetuksenLisäopetuksenOpiskeluoikeus(a) ||
  isPerusopetuksenOpiskeluoikeus(a) ||
  isTaiteenPerusopetuksenOpiskeluoikeus(a) ||
  isTutkintokoulutukseenValmentavanOpiskeluoikeus(a) ||
  isVapaanSivistystyönOpiskeluoikeus(a) ||
  isYlioppilastutkinnonOpiskeluoikeus(a)
