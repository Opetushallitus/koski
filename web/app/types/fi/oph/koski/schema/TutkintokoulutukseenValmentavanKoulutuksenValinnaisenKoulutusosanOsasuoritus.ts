import { LocalizedString } from './LocalizedString'
import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusViikoissa } from './LaajuusViikoissa'

/**
 * TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus`
 */
export type TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus =
  {
    $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus'
    nimi: LocalizedString
    tunniste: PaikallinenKoodi
    laajuus?: LaajuusViikoissa
  }

export const TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus =
  (o: {
    nimi: LocalizedString
    tunniste: PaikallinenKoodi
    laajuus?: LaajuusViikoissa
  }): TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus => ({
    $class:
      'fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus',
    ...o
  })

export const isTutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus =>
    a?.$class ===
    'TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus'
