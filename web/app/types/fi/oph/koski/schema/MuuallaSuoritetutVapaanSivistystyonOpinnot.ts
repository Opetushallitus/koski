import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * MuuallaSuoritetutVapaanSivistystyönOpinnot
 *
 * @see `fi.oph.koski.schema.MuuallaSuoritetutVapaanSivistystyönOpinnot`
 */
export type MuuallaSuoritetutVapaanSivistystyönOpinnot = {
  $class: 'fi.oph.koski.schema.MuuallaSuoritetutVapaanSivistystyönOpinnot'
  tunniste: Koodistokoodiviite<'vstmuuallasuoritetutopinnot', string>
  kuvaus: LocalizedString
  laajuus: LaajuusOpintopisteissä
}

export const MuuallaSuoritetutVapaanSivistystyönOpinnot = (o: {
  tunniste: Koodistokoodiviite<'vstmuuallasuoritetutopinnot', string>
  kuvaus: LocalizedString
  laajuus: LaajuusOpintopisteissä
}): MuuallaSuoritetutVapaanSivistystyönOpinnot => ({
  $class: 'fi.oph.koski.schema.MuuallaSuoritetutVapaanSivistystyönOpinnot',
  ...o
})

MuuallaSuoritetutVapaanSivistystyönOpinnot.className =
  'fi.oph.koski.schema.MuuallaSuoritetutVapaanSivistystyönOpinnot' as const

export const isMuuallaSuoritetutVapaanSivistystyönOpinnot = (
  a: any
): a is MuuallaSuoritetutVapaanSivistystyönOpinnot =>
  a?.$class === 'fi.oph.koski.schema.MuuallaSuoritetutVapaanSivistystyönOpinnot'
