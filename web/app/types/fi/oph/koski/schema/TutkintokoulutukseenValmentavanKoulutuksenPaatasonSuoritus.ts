import {
  TutkintokoulutukseenValmentavanKoulutuksenSuoritus,
  isTutkintokoulutukseenValmentavanKoulutuksenSuoritus
} from './TutkintokoulutukseenValmentavanKoulutuksenSuoritus'

/**
 * TutkintokoulutukseenValmentavanKoulutuksenPäätasonSuoritus
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenPäätasonSuoritus`
 */
export type TutkintokoulutukseenValmentavanKoulutuksenPäätasonSuoritus =
  TutkintokoulutukseenValmentavanKoulutuksenSuoritus

export const isTutkintokoulutukseenValmentavanKoulutuksenPäätasonSuoritus = (
  a: any
): a is TutkintokoulutukseenValmentavanKoulutuksenPäätasonSuoritus =>
  isTutkintokoulutukseenValmentavanKoulutuksenSuoritus(a)
