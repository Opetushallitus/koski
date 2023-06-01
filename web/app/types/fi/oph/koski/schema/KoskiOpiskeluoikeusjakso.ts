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
  EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso,
  isEuropeanSchoolOfHelsinkiOpiskeluoikeusjakso
} from './EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso'
import {
  InternationalSchoolOpiskeluoikeusjakso,
  isInternationalSchoolOpiskeluoikeusjakso
} from './InternationalSchoolOpiskeluoikeusjakso'
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
  | EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso
  | InternationalSchoolOpiskeluoikeusjakso
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
  isEuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(a) ||
  isInternationalSchoolOpiskeluoikeusjakso(a) ||
  isLukionOpiskeluoikeusjakso(a) ||
  isMuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso(a) ||
  isNuortenPerusopetuksenOpiskeluoikeusjakso(a) ||
  isPerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso(a) ||
  isTaiteenPerusopetuksenOpiskeluoikeusjakso(a) ||
  isTutkintokoulutukseenValmentavanOpiskeluoikeusjakso(a) ||
  isVapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso(a) ||
  isYlioppilastutkinnonOpiskeluoikeusjakso(a)
