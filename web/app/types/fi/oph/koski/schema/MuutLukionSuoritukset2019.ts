import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Kategoria moduuleille ja opintojaksoille, jotka eivät liity suoraan mihinkään yksittäiseen oppiaineeseen.
 *
 * @see `fi.oph.koski.schema.MuutLukionSuoritukset2019`
 */
export type MuutLukionSuoritukset2019 = {
  $class: 'fi.oph.koski.schema.MuutLukionSuoritukset2019'
  tunniste: Koodistokoodiviite<'lukionmuutopinnot', 'MS'>
  laajuus?: LaajuusOpintopisteissä
}

export const MuutLukionSuoritukset2019 = (
  o: {
    tunniste?: Koodistokoodiviite<'lukionmuutopinnot', 'MS'>
    laajuus?: LaajuusOpintopisteissä
  } = {}
): MuutLukionSuoritukset2019 => ({
  $class: 'fi.oph.koski.schema.MuutLukionSuoritukset2019',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'MS',
    koodistoUri: 'lukionmuutopinnot'
  }),
  ...o
})

export const isMuutLukionSuoritukset2019 = (
  a: any
): a is MuutLukionSuoritukset2019 =>
  a?.$class === 'fi.oph.koski.schema.MuutLukionSuoritukset2019'
