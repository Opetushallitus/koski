import {
  SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi,
  isSanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi
} from './SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi'

/**
 * TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi`
 */
export type TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi =
  SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi

export const isTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi =>
    isSanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi(
      a
    )
