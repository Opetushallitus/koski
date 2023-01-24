import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusKaikkiYksiköt } from './LaajuusKaikkiYksikot'
import { LocalizedString } from './LocalizedString'

/**
 * TutkinnonOsaaPienempiKokonaisuus
 *
 * @see `fi.oph.koski.schema.TutkinnonOsaaPienempiKokonaisuus`
 */
export type TutkinnonOsaaPienempiKokonaisuus = {
  $class: 'fi.oph.koski.schema.TutkinnonOsaaPienempiKokonaisuus'
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus: LocalizedString
}

export const TutkinnonOsaaPienempiKokonaisuus = (o: {
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus: LocalizedString
}): TutkinnonOsaaPienempiKokonaisuus => ({
  $class: 'fi.oph.koski.schema.TutkinnonOsaaPienempiKokonaisuus',
  ...o
})

TutkinnonOsaaPienempiKokonaisuus.className =
  'fi.oph.koski.schema.TutkinnonOsaaPienempiKokonaisuus' as const

export const isTutkinnonOsaaPienempiKokonaisuus = (
  a: any
): a is TutkinnonOsaaPienempiKokonaisuus =>
  a?.$class === 'fi.oph.koski.schema.TutkinnonOsaaPienempiKokonaisuus'
