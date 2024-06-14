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
  EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot,
  isEuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot
} from './EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisatiedot'
import {
  InternationalSchoolOpiskeluoikeudenLisätiedot,
  isInternationalSchoolOpiskeluoikeudenLisätiedot
} from './InternationalSchoolOpiskeluoikeudenLisatiedot'
import {
  LukionOpiskeluoikeudenLisätiedot,
  isLukionOpiskeluoikeudenLisätiedot
} from './LukionOpiskeluoikeudenLisatiedot'
import {
  LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot,
  isLukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot
} from './LukioonValmistavanKoulutuksenOpiskeluoikeudenLisatiedot'
import {
  PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot,
  isPerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot
} from './PerusopetuksenLisaopetuksenOpiskeluoikeudenLisatiedot'
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

/**
 * MaksuttomuusTieto
 *
 * @see `fi.oph.koski.schema.MaksuttomuusTieto`
 */
export type MaksuttomuusTieto =
  | AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot
  | AmmatillisenOpiskeluoikeudenLisätiedot
  | DIAOpiskeluoikeudenLisätiedot
  | EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot
  | InternationalSchoolOpiskeluoikeudenLisätiedot
  | LukionOpiskeluoikeudenLisätiedot
  | LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot
  | PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot
  | TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot
  | TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot
  | TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot
  | VapaanSivistystyönOpiskeluoikeudenLisätiedot

export const isMaksuttomuusTieto = (a: any): a is MaksuttomuusTieto =>
  isAikuistenPerusopetuksenOpiskeluoikeudenLisätiedot(a) ||
  isAmmatillisenOpiskeluoikeudenLisätiedot(a) ||
  isDIAOpiskeluoikeudenLisätiedot(a) ||
  isEuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot(a) ||
  isInternationalSchoolOpiskeluoikeudenLisätiedot(a) ||
  isLukionOpiskeluoikeudenLisätiedot(a) ||
  isLukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot(a) ||
  isPerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(a) ||
  isTutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot(
    a
  ) ||
  isTutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot(
    a
  ) ||
  isTutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot(
    a
  ) ||
  isVapaanSivistystyönOpiskeluoikeudenLisätiedot(a)
