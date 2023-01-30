import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * Ammatillisen tutkinnon osaa pienemmän kokonaisuuden tunnistetiedot
 *
 * @see `fi.oph.koski.schema.AmmatillisenTutkinnonOsaaPienempiKokonaisuus`
 */
export type AmmatillisenTutkinnonOsaaPienempiKokonaisuus = {
  $class: 'fi.oph.koski.schema.AmmatillisenTutkinnonOsaaPienempiKokonaisuus'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
}

export const AmmatillisenTutkinnonOsaaPienempiKokonaisuus = (o: {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
}): AmmatillisenTutkinnonOsaaPienempiKokonaisuus => ({
  $class: 'fi.oph.koski.schema.AmmatillisenTutkinnonOsaaPienempiKokonaisuus',
  ...o
})

AmmatillisenTutkinnonOsaaPienempiKokonaisuus.className =
  'fi.oph.koski.schema.AmmatillisenTutkinnonOsaaPienempiKokonaisuus' as const

export const isAmmatillisenTutkinnonOsaaPienempiKokonaisuus = (
  a: any
): a is AmmatillisenTutkinnonOsaaPienempiKokonaisuus =>
  a?.$class ===
  'fi.oph.koski.schema.AmmatillisenTutkinnonOsaaPienempiKokonaisuus'
