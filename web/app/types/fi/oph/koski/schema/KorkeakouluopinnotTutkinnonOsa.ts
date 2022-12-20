import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * KorkeakouluopinnotTutkinnonOsa
 *
 * @see `fi.oph.koski.schema.KorkeakouluopinnotTutkinnonOsa`
 */
export type KorkeakouluopinnotTutkinnonOsa = {
  $class: 'fi.oph.koski.schema.KorkeakouluopinnotTutkinnonOsa'
  tunniste: Koodistokoodiviite<'tutkinnonosatvalinnanmahdollisuus', '2'>
  laajuus?: LaajuusOsaamispisteissä
}

export const KorkeakouluopinnotTutkinnonOsa = (
  o: {
    tunniste?: Koodistokoodiviite<'tutkinnonosatvalinnanmahdollisuus', '2'>
    laajuus?: LaajuusOsaamispisteissä
  } = {}
): KorkeakouluopinnotTutkinnonOsa => ({
  $class: 'fi.oph.koski.schema.KorkeakouluopinnotTutkinnonOsa',
  tunniste: Koodistokoodiviite({
    koodiarvo: '2',
    koodistoUri: 'tutkinnonosatvalinnanmahdollisuus'
  }),
  ...o
})

export const isKorkeakouluopinnotTutkinnonOsa = (
  a: any
): a is KorkeakouluopinnotTutkinnonOsa =>
  a?.$class === 'fi.oph.koski.schema.KorkeakouluopinnotTutkinnonOsa'
