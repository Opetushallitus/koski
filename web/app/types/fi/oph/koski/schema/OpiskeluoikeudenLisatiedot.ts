import {
  AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot,
  isAikuistenPerusopetuksenOpiskeluoikeudenLisätiedot
} from './AikuistenPerusopetuksenOpiskeluoikeudenLisatiedot'
import {
  AmmatillisenOpiskeluoikeudenLisätiedot,
  isAmmatillisenOpiskeluoikeudenLisätiedot
} from './AmmatillisenOpiskeluoikeudenLisatiedot'
import {
  DIAOpiskeluoikeudenLisätiedot,
  isDIAOpiskeluoikeudenLisätiedot
} from './DIAOpiskeluoikeudenLisatiedot'
import {
  EsiopetuksenOpiskeluoikeudenLisätiedot,
  isEsiopetuksenOpiskeluoikeudenLisätiedot
} from './EsiopetuksenOpiskeluoikeudenLisatiedot'
import {
  EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot,
  isEuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot
} from './EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisatiedot'
import {
  InternationalSchoolOpiskeluoikeudenLisätiedot,
  isInternationalSchoolOpiskeluoikeudenLisätiedot
} from './InternationalSchoolOpiskeluoikeudenLisatiedot'
import {
  KorkeakoulunOpiskeluoikeudenLisätiedot,
  isKorkeakoulunOpiskeluoikeudenLisätiedot
} from './KorkeakoulunOpiskeluoikeudenLisatiedot'
import {
  LukionOpiskeluoikeudenLisätiedot,
  isLukionOpiskeluoikeudenLisätiedot
} from './LukionOpiskeluoikeudenLisatiedot'
import {
  LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot,
  isLukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot
} from './LukioonValmistavanKoulutuksenOpiskeluoikeudenLisatiedot'
import {
  MuunKuinSäännellynKoulutuksenLisätiedot,
  isMuunKuinSäännellynKoulutuksenLisätiedot
} from './MuunKuinSaannellynKoulutuksenLisatiedot'
import {
  PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot,
  isPerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot
} from './PerusopetuksenLisaopetuksenOpiskeluoikeudenLisatiedot'
import {
  PerusopetuksenOpiskeluoikeudenLisätiedot,
  isPerusopetuksenOpiskeluoikeudenLisätiedot
} from './PerusopetuksenOpiskeluoikeudenLisatiedot'
import {
  TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot,
  isTutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot
} from './TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisatiedot'
import {
  TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot,
  isTutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot
} from './TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisatiedot'
import {
  TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot,
  isTutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot
} from './TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisatiedot'
import {
  VapaanSivistystyönOpiskeluoikeudenLisätiedot,
  isVapaanSivistystyönOpiskeluoikeudenLisätiedot
} from './VapaanSivistystyonOpiskeluoikeudenLisatiedot'
import {
  YlioppilastutkinnonOpiskeluoikeudenLisätiedot,
  isYlioppilastutkinnonOpiskeluoikeudenLisätiedot
} from './YlioppilastutkinnonOpiskeluoikeudenLisatiedot'

/**
 * OpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.schema.OpiskeluoikeudenLisätiedot`
 */
export type OpiskeluoikeudenLisätiedot =
  | AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot
  | AmmatillisenOpiskeluoikeudenLisätiedot
  | DIAOpiskeluoikeudenLisätiedot
  | EsiopetuksenOpiskeluoikeudenLisätiedot
  | EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot
  | InternationalSchoolOpiskeluoikeudenLisätiedot
  | KorkeakoulunOpiskeluoikeudenLisätiedot
  | LukionOpiskeluoikeudenLisätiedot
  | LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot
  | MuunKuinSäännellynKoulutuksenLisätiedot
  | PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot
  | PerusopetuksenOpiskeluoikeudenLisätiedot
  | TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot
  | TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot
  | TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot
  | VapaanSivistystyönOpiskeluoikeudenLisätiedot
  | YlioppilastutkinnonOpiskeluoikeudenLisätiedot

export const isOpiskeluoikeudenLisätiedot = (
  a: any
): a is OpiskeluoikeudenLisätiedot =>
  isAikuistenPerusopetuksenOpiskeluoikeudenLisätiedot(a) ||
  isAmmatillisenOpiskeluoikeudenLisätiedot(a) ||
  isDIAOpiskeluoikeudenLisätiedot(a) ||
  isEsiopetuksenOpiskeluoikeudenLisätiedot(a) ||
  isEuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot(a) ||
  isInternationalSchoolOpiskeluoikeudenLisätiedot(a) ||
  isKorkeakoulunOpiskeluoikeudenLisätiedot(a) ||
  isLukionOpiskeluoikeudenLisätiedot(a) ||
  isLukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot(a) ||
  isMuunKuinSäännellynKoulutuksenLisätiedot(a) ||
  isPerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(a) ||
  isPerusopetuksenOpiskeluoikeudenLisätiedot(a) ||
  isTutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot(
    a
  ) ||
  isTutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot(
    a
  ) ||
  isTutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot(
    a
  ) ||
  isVapaanSivistystyönOpiskeluoikeudenLisätiedot(a) ||
  isYlioppilastutkinnonOpiskeluoikeudenLisätiedot(a)
