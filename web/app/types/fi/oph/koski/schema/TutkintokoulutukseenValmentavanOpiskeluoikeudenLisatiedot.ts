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

/**
 * TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot`
 */
export type TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot =
  | TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot
  | TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot
  | TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot

export const isTutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot = (
  a: any
): a is TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot =>
  isTutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot(
    a
  ) ||
  isTutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot(
    a
  ) ||
  isTutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot(
    a
  )
