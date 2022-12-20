import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Kategoria temaattisille opinnoille 2019.
 *
 * @see `fi.oph.koski.schema.TemaattisetOpinnot2019`
 */
export type TemaattisetOpinnot2019 = {
  $class: 'fi.oph.koski.schema.TemaattisetOpinnot2019'
  tunniste: Koodistokoodiviite<'lukionmuutopinnot', 'TO'>
  laajuus?: LaajuusOpintopisteissä
}

export const TemaattisetOpinnot2019 = (
  o: {
    tunniste?: Koodistokoodiviite<'lukionmuutopinnot', 'TO'>
    laajuus?: LaajuusOpintopisteissä
  } = {}
): TemaattisetOpinnot2019 => ({
  $class: 'fi.oph.koski.schema.TemaattisetOpinnot2019',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'TO',
    koodistoUri: 'lukionmuutopinnot'
  }),
  ...o
})

export const isTemaattisetOpinnot2019 = (a: any): a is TemaattisetOpinnot2019 =>
  a?.$class === 'fi.oph.koski.schema.TemaattisetOpinnot2019'
