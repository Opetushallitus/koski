import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Tutkintokoulutukseen valmentavan koulutuksen osasuorituksen hyväksytty/hylätty arviointi
 *
 * @see `fi.oph.koski.schema.SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi`
 */
export type SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi =
  {
    $class: 'fi.oph.koski.schema.SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi'
    arvosana: Koodistokoodiviite<
      'arviointiasteikkotuva',
      'Hyväksytty' | 'Hylätty'
    >
    kuvaus?: LocalizedString
    päivä: string
    hyväksytty?: boolean
  }

export const SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi =
  (o: {
    arvosana: Koodistokoodiviite<
      'arviointiasteikkotuva',
      'Hyväksytty' | 'Hylätty'
    >
    kuvaus?: LocalizedString
    päivä: string
    hyväksytty?: boolean
  }): SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi => ({
    $class:
      'fi.oph.koski.schema.SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi',
    ...o
  })

export const isSanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi =
  (
    a: any
  ): a is SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi =>
    a?.$class ===
    'fi.oph.koski.schema.SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi'
