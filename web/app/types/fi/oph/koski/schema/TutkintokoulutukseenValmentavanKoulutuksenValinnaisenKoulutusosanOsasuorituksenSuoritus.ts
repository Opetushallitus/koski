import { TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi } from './TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus } from './TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'

/**
 * Valinnaisten tutkintokoulutukseen valmentavan koulutuksen opintojen osasuoritukset. Tutkintokoulutukseen valmentavan valinnaisen koulutuksen osan paikalliset osasuoritukset, joilla on laajuus viikkoina sekä arvosana.
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus`
 */
export type TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus =
  {
    $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus'
    arviointi?: Array<TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'tutkintokoulutukseenvalmentava'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus
    tunnustettu?: OsaamisenTunnustaminen
  }

export const TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus =
  (o: {
    arviointi?: Array<TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi>
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'tutkintokoulutukseenvalmentava'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus
    tunnustettu?: OsaamisenTunnustaminen
  }): TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'tutkintokoulutukseenvalmentava',
      koodistoUri: 'suorituksentyyppi'
    }),
    $class:
      'fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus',
    ...o
  })

export const isTutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus =>
    a?.$class ===
    'TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus'
