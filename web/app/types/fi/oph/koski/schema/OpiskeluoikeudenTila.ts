import {
  AikuistenPerusopetuksenOpiskeluoikeudenTila,
  isAikuistenPerusopetuksenOpiskeluoikeudenTila
} from './AikuistenPerusopetuksenOpiskeluoikeudenTila'
import {
  AmmatillinenOpiskeluoikeudenTila,
  isAmmatillinenOpiskeluoikeudenTila
} from './AmmatillinenOpiskeluoikeudenTila'
import {
  DIAOpiskeluoikeudenTila,
  isDIAOpiskeluoikeudenTila
} from './DIAOpiskeluoikeudenTila'
import {
  EBOpiskeluoikeudenTila,
  isEBOpiskeluoikeudenTila
} from './EBOpiskeluoikeudenTila'
import {
  EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila,
  isEuropeanSchoolOfHelsinkiOpiskeluoikeudenTila
} from './EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila'
import {
  InternationalSchoolOpiskeluoikeudenTila,
  isInternationalSchoolOpiskeluoikeudenTila
} from './InternationalSchoolOpiskeluoikeudenTila'
import {
  KielitutkinnonOpiskeluoikeudenTila,
  isKielitutkinnonOpiskeluoikeudenTila
} from './KielitutkinnonOpiskeluoikeudenTila'
import {
  KorkeakoulunOpiskeluoikeudenTila,
  isKorkeakoulunOpiskeluoikeudenTila
} from './KorkeakoulunOpiskeluoikeudenTila'
import {
  LukionOpiskeluoikeudenTila,
  isLukionOpiskeluoikeudenTila
} from './LukionOpiskeluoikeudenTila'
import {
  MuunKuinSäännellynKoulutuksenTila,
  isMuunKuinSäännellynKoulutuksenTila
} from './MuunKuinSaannellynKoulutuksenTila'
import {
  NuortenPerusopetuksenOpiskeluoikeudenTila,
  isNuortenPerusopetuksenOpiskeluoikeudenTila
} from './NuortenPerusopetuksenOpiskeluoikeudenTila'
import {
  PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila,
  isPerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila
} from './PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila'
import {
  TaiteenPerusopetuksenOpiskeluoikeudenTila,
  isTaiteenPerusopetuksenOpiskeluoikeudenTila
} from './TaiteenPerusopetuksenOpiskeluoikeudenTila'
import {
  TutkintokoulutukseenValmentavanOpiskeluoikeudenTila,
  isTutkintokoulutukseenValmentavanOpiskeluoikeudenTila
} from './TutkintokoulutukseenValmentavanOpiskeluoikeudenTila'
import {
  VapaanSivistystyönOpiskeluoikeudenTila,
  isVapaanSivistystyönOpiskeluoikeudenTila
} from './VapaanSivistystyonOpiskeluoikeudenTila'
import {
  YlioppilastutkinnonOpiskeluoikeudenTila,
  isYlioppilastutkinnonOpiskeluoikeudenTila
} from './YlioppilastutkinnonOpiskeluoikeudenTila'

/**
 * OpiskeluoikeudenTila
 *
 * @see `fi.oph.koski.schema.OpiskeluoikeudenTila`
 */
export type OpiskeluoikeudenTila =
  | AikuistenPerusopetuksenOpiskeluoikeudenTila
  | AmmatillinenOpiskeluoikeudenTila
  | DIAOpiskeluoikeudenTila
  | EBOpiskeluoikeudenTila
  | EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila
  | InternationalSchoolOpiskeluoikeudenTila
  | KielitutkinnonOpiskeluoikeudenTila
  | KorkeakoulunOpiskeluoikeudenTila
  | LukionOpiskeluoikeudenTila
  | MuunKuinSäännellynKoulutuksenTila
  | NuortenPerusopetuksenOpiskeluoikeudenTila
  | PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila
  | TaiteenPerusopetuksenOpiskeluoikeudenTila
  | TutkintokoulutukseenValmentavanOpiskeluoikeudenTila
  | VapaanSivistystyönOpiskeluoikeudenTila
  | YlioppilastutkinnonOpiskeluoikeudenTila

export const isOpiskeluoikeudenTila = (a: any): a is OpiskeluoikeudenTila =>
  isAikuistenPerusopetuksenOpiskeluoikeudenTila(a) ||
  isAmmatillinenOpiskeluoikeudenTila(a) ||
  isDIAOpiskeluoikeudenTila(a) ||
  isEBOpiskeluoikeudenTila(a) ||
  isEuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(a) ||
  isInternationalSchoolOpiskeluoikeudenTila(a) ||
  isKielitutkinnonOpiskeluoikeudenTila(a) ||
  isKorkeakoulunOpiskeluoikeudenTila(a) ||
  isLukionOpiskeluoikeudenTila(a) ||
  isMuunKuinSäännellynKoulutuksenTila(a) ||
  isNuortenPerusopetuksenOpiskeluoikeudenTila(a) ||
  isPerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila(a) ||
  isTaiteenPerusopetuksenOpiskeluoikeudenTila(a) ||
  isTutkintokoulutukseenValmentavanOpiskeluoikeudenTila(a) ||
  isVapaanSivistystyönOpiskeluoikeudenTila(a) ||
  isYlioppilastutkinnonOpiskeluoikeudenTila(a)
