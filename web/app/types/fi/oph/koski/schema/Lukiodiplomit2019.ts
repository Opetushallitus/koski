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

Lukiodiplomit2019.className = 'fi.oph.koski.schema.Lukiodiplomit2019' as const

export const isLukiodiplomit2019 = (a: any): a is Lukiodiplomit2019 =>
  a?.$class === 'fi.oph.koski.schema.Lukiodiplomit2019'
