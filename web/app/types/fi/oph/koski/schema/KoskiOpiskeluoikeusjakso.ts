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
  KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso,
  isKielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso
} from './KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso'
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
  YlioppilastutkinnonOpiskeluoikeusjakso,
  isYlioppilastutkinnonOpiskeluoikeusjakso
} from './YlioppilastutkinnonOpiskeluoikeusjakso'

/**
 * KoskiOpiskeluoikeusjakso
 *
 * @see `fi.oph.koski.schema.KoskiOpiskeluoikeusjakso`
 */
export type KoskiOpiskeluoikeusjakso =
  | AikuistenPerusopetuksenOpiskeluoikeusjakso
  | AmmatillinenOpiskeluoikeusjakso
  | DIAOpiskeluoikeusjakso
  | EBOpiskeluoikeusjakso
  | EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso
  | InternationalSchoolOpiskeluoikeusjakso
  | KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso
  | LukionOpiskeluoikeusjakso
  | MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso
  | NuortenPerusopetuksenOpiskeluoikeusjakso
  | PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso
  | TaiteenPerusopetuksenOpiskeluoikeusjakso
  | TutkintokoulutukseenValmentavanOpiskeluoikeusjakso
  | VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso
  | YlioppilastutkinnonOpiskeluoikeusjakso

export const isKoskiOpiskeluoikeusjakso = (
  a: any
): a is KoskiOpiskeluoikeusjakso =>
  isAikuistenPerusopetuksenOpiskeluoikeusjakso(a) ||
  isAmmatillinenOpiskeluoikeusjakso(a) ||
  isDIAOpiskeluoikeusjakso(a) ||
  isEBOpiskeluoikeusjakso(a) ||
  isEuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(a) ||
  isInternationalSchoolOpiskeluoikeusjakso(a) ||
  isKielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso(a) ||
  isLukionOpiskeluoikeusjakso(a) ||
  isMuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso(a) ||
  isNuortenPerusopetuksenOpiskeluoikeusjakso(a) ||
  isPerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso(a) ||
  isTaiteenPerusopetuksenOpiskeluoikeusjakso(a) ||
  isTutkintokoulutukseenValmentavanOpiskeluoikeusjakso(a) ||
  isVapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso(a) ||
  isYlioppilastutkinnonOpiskeluoikeusjakso(a)
