import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * Korkeakouluopintojen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus`
 */
export type KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus = {
  $class: 'fi.oph.koski.schema.KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
}

export const KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus = (o: {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
}): KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus => ({
  $class:
    'fi.oph.koski.schema.KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus',
  ...o
})

export const isKorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus = (
  a: any
): a is KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus =>
  a?.$class ===
  'fi.oph.koski.schema.KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus'
