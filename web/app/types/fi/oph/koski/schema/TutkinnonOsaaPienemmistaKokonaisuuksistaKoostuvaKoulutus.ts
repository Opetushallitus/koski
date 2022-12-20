import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusKaikkiYksiköt } from './LaajuusKaikkiYksikot'
import { LocalizedString } from './LocalizedString'

/**
 * TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus
 *
 * @see `fi.oph.koski.schema.TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus`
 */
export type TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus = {
  $class: 'fi.oph.koski.schema.TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus'
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus: LocalizedString
}

export const TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus = (o: {
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus: LocalizedString
}): TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus => ({
  $class:
    'fi.oph.koski.schema.TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus',
  ...o
})

export const isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus = (
  a: any
): a is TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus =>
  a?.$class ===
  'fi.oph.koski.schema.TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus'
