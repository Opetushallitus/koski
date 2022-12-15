import {
  TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus,
  isTutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus
} from './TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus'
import {
  TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus,
  isTutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus
} from './TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus'

/**
 * TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus`
 */
export type TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus =
  | TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus
  | TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus

export const isTutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus = (
  a: any
): a is TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus =>
  isTutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus(a) ||
  isTutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus(a)
