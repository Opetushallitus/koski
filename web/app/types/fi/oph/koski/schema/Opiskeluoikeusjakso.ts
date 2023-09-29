import {
  AikuistenPerusopetuksenOpiskeluoikeusjakso,
  isAikuistenPerusopetuksenOpiskeluoikeusjakso
} from './AikuistenPerusopetuksenOpiskeluoikeusjakso'
import {
  AmmatillinenOpiskeluoikeusjakso,
  isAmmatillinenOpiskeluoikeusjakso
} from './AmmatillinenOpiskeluoikeusjakso'
import {
  DIAOpiskeluoikeusjakso,
  isDIAOpiskeluoikeusjakso
} from './DIAOpiskeluoikeusjakso'
import {
  EBOpiskeluoikeusjakso,
  isEBOpiskeluoikeusjakso
} from './EBOpiskeluoikeusjakso'
import {
  EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso,
  isEuropeanSchoolOfHelsinkiOpiskeluoikeusjakso
} from './EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso'
import {
  InternationalSchoolOpiskeluoikeusjakso,
  isInternationalSchoolOpiskeluoikeusjakso
} from './InternationalSchoolOpiskeluoikeusjakso'
import {
  KorkeakoulunOpiskeluoikeusjakso,
  isKorkeakoulunOpiskeluoikeusjakso
} from './KorkeakoulunOpiskeluoikeusjakso'
import {
  LukionOpiskeluoikeusjakso,
  isLukionOpiskeluoikeusjakso
} from './LukionOpiskeluoikeusjakso'
import {
  MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso,
  isMuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso
} from './MuunKuinSaannellynKoulutuksenOpiskeluoikeudenJakso'
import {
  NuortenPerusopetuksenOpiskeluoikeusjakso,
  isNuortenPerusopetuksenOpiskeluoikeusjakso
} from './NuortenPerusopetuksenOpiskeluoikeusjakso'
import {
  OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso,
  isOppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso
} from './OppivelvollisilleSuunnattuVapaanSivistystyonOpiskeluoikeusjakso'
import {
  PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso,
  isPerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso
} from './PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso'
import {
  TaiteenPerusopetuksenOpiskeluoikeusjakso,
  isTaiteenPerusopetuksenOpiskeluoikeusjakso
} from './TaiteenPerusopetuksenOpiskeluoikeusjakso'
import {
  TutkintokoulutukseenValmentavanOpiskeluoikeusjakso,
  isTutkintokoulutukseenValmentavanOpiskeluoikeusjakso
} from './TutkintokoulutukseenValmentavanOpiskeluoikeusjakso'
import {
  VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso,
  isVapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso
} from './VapaanSivistystyonJotpaKoulutuksenOpiskeluoikeusjakso'
import {
  VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso,
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso
} from './VapaanSivistystyonVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso'
import {
  YlioppilastutkinnonOpiskeluoikeusjakso,
  isYlioppilastutkinnonOpiskeluoikeusjakso
} from './YlioppilastutkinnonOpiskeluoikeusjakso'

/**
 * Opiskeluoikeusjakso
 *
 * @see `fi.oph.koski.schema.Opiskeluoikeusjakso`
 */
export type Opiskeluoikeusjakso =
  | AikuistenPerusopetuksenOpiskeluoikeusjakso
  | AmmatillinenOpiskeluoikeusjakso
  | DIAOpiskeluoikeusjakso
  | EBOpiskeluoikeusjakso
  | EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso
  | InternationalSchoolOpiskeluoikeusjakso
  | KorkeakoulunOpiskeluoikeusjakso
  | LukionOpiskeluoikeusjakso
  | MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso
  | NuortenPerusopetuksenOpiskeluoikeusjakso
  | OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso
  | PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso
  | TaiteenPerusopetuksenOpiskeluoikeusjakso
  | TutkintokoulutukseenValmentavanOpiskeluoikeusjakso
  | VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso
  | VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso
  | YlioppilastutkinnonOpiskeluoikeusjakso

export const isOpiskeluoikeusjakso = (a: any): a is Opiskeluoikeusjakso =>
  isAikuistenPerusopetuksenOpiskeluoikeusjakso(a) ||
  isAmmatillinenOpiskeluoikeusjakso(a) ||
  isDIAOpiskeluoikeusjakso(a) ||
  isEBOpiskeluoikeusjakso(a) ||
  isEuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(a) ||
  isInternationalSchoolOpiskeluoikeusjakso(a) ||
  isKorkeakoulunOpiskeluoikeusjakso(a) ||
  isLukionOpiskeluoikeusjakso(a) ||
  isMuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso(a) ||
  isNuortenPerusopetuksenOpiskeluoikeusjakso(a) ||
  isOppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(a) ||
  isPerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso(a) ||
  isTaiteenPerusopetuksenOpiskeluoikeusjakso(a) ||
  isTutkintokoulutukseenValmentavanOpiskeluoikeusjakso(a) ||
  isVapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso(a) ||
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso(a) ||
  isYlioppilastutkinnonOpiskeluoikeusjakso(a)
