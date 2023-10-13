import {
  AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus,
  isAktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus
} from './AktiivisetJaPaattyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus'
import {
  AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus,
  isAktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus
} from './AktiivisetJaPaattyneetOpinnotAmmatillinenOpiskeluoikeus'
import {
  AktiivisetJaPäättyneetOpinnotDIAOpiskeluoikeus,
  isAktiivisetJaPäättyneetOpinnotDIAOpiskeluoikeus
} from './AktiivisetJaPaattyneetOpinnotDIAOpiskeluoikeus'
import {
  AktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus,
  isAktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus
} from './AktiivisetJaPaattyneetOpinnotEBTutkinnonOpiskeluoikeus'
import {
  AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus,
  isAktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus
} from './AktiivisetJaPaattyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus'
import {
  AktiivisetJaPäättyneetOpinnotIBOpiskeluoikeus,
  isAktiivisetJaPäättyneetOpinnotIBOpiskeluoikeus
} from './AktiivisetJaPaattyneetOpinnotIBOpiskeluoikeus'
import {
  AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus,
  isAktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus
} from './AktiivisetJaPaattyneetOpinnotInternationalSchoolOpiskeluoikeus'
import {
  AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus,
  isAktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus
} from './AktiivisetJaPaattyneetOpinnotLukionOpiskeluoikeus'
import {
  AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus,
  isAktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus
} from './AktiivisetJaPaattyneetOpinnotMuunKuinSaannellynKoulutuksenOpiskeluoikeus'
import {
  AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus,
  isAktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus
} from './AktiivisetJaPaattyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus'
import {
  AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus,
  isAktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus
} from './AktiivisetJaPaattyneetOpinnotVapaanSivistystyonOpiskeluoikeus'

/**
 * AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus`
 */
export type AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus =
  | AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus
  | AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus
  | AktiivisetJaPäättyneetOpinnotDIAOpiskeluoikeus
  | AktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus
  | AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus
  | AktiivisetJaPäättyneetOpinnotIBOpiskeluoikeus
  | AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus
  | AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus
  | AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus
  | AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus
  | AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus

export const isAktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus =>
    isAktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus(a) ||
    isAktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus(a) ||
    isAktiivisetJaPäättyneetOpinnotDIAOpiskeluoikeus(a) ||
    isAktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus(a) ||
    isAktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus(a) ||
    isAktiivisetJaPäättyneetOpinnotIBOpiskeluoikeus(a) ||
    isAktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus(a) ||
    isAktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus(a) ||
    isAktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus(
      a
    ) ||
    isAktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus(
      a
    ) ||
    isAktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus(a)
