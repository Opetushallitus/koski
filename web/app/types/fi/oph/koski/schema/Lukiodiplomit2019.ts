import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Kategoria lukiodiplomeille 2019.
 *
 * @see `fi.oph.koski.schema.Lukiodiplomit2019`
 */
export type Lukiodiplomit2019 = {
  $class: 'fi.oph.koski.schema.Lukiodiplomit2019'
  tunniste: Koodistokoodiviite<'lukionmuutopinnot', 'LD'>
  laajuus?: LaajuusOpintopisteissä
}

export const Lukiodiplomit2019 = (
  o: {
    tunniste?: Koodistokoodiviite<'lukionmuutopinnot', 'LD'>
    laajuus?: LaajuusOpintopisteissä
  } = {}
): Lukiodiplomit2019 => ({
  $class: 'fi.oph.koski.schema.Lukiodiplomit2019',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'LD',
    koodistoUri: 'lukionmuutopinnot'
  }),
  ...o
})

export const isLukiodiplomit2019 = (a: any): a is Lukiodiplomit2019 =>
  a?.$class === 'fi.oph.koski.schema.Lukiodiplomit2019'
