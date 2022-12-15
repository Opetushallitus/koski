import { SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi } from './SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa } from './TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'
import { TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus } from './TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus'

/**
 * Tutkintokoulutukseen valmentavan koulutuksen valinnaisten opintojen osasuoritus
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus`
 */
export type TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus =
  {
    $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus'
    arviointi?: Array<SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'tutkintokoulutukseenvalmentava'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa
    tunnustettu?: OsaamisenTunnustaminen
    osasuoritukset?: Array<TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus>
  }

export const TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus =
  (
    o: {
      arviointi?: Array<SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi>
      tyyppi?: Koodistokoodiviite<
        'suorituksentyyppi',
        'tutkintokoulutukseenvalmentava'
      >
      tila?: Koodistokoodiviite<'suorituksentila', string>
      suorituskieli?: Koodistokoodiviite<'kieli', string>
      koulutusmoduuli?: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa
      tunnustettu?: OsaamisenTunnustaminen
      osasuoritukset?: Array<TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus>
    } = {}
  ): TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'tutkintokoulutukseenvalmentava',
      koodistoUri: 'suorituksentyyppi'
    }),
    koulutusmoduuli:
      TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa({
        tunniste: Koodistokoodiviite({
          koodiarvo: '104',
          koodistoUri: 'koulutuksenosattuva'
        })
      }),
    $class:
      'fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus',
    ...o
  })

export const isTutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus =>
    a?.$class ===
    'TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus'
