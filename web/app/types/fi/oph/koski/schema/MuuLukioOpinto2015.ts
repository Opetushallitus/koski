import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusKursseissa } from './LaajuusKursseissa'

/**
 * Kategoria kursseille, jotka eivät liity suoraan mihinkään yksittäiseen oppiaineeseen. Esimerkiksi lukiodiplomi, taiteiden väliset opinnot, teemaopinnot
 *
 * @see `fi.oph.koski.schema.MuuLukioOpinto2015`
 */
export type MuuLukioOpinto2015 = {
  $class: 'fi.oph.koski.schema.MuuLukioOpinto2015'
  tunniste: Koodistokoodiviite<'lukionmuutopinnot', string>
  laajuus?: LaajuusKursseissa
}

export const MuuLukioOpinto2015 = (o: {
  tunniste: Koodistokoodiviite<'lukionmuutopinnot', string>
  laajuus?: LaajuusKursseissa
}): MuuLukioOpinto2015 => ({
  $class: 'fi.oph.koski.schema.MuuLukioOpinto2015',
  ...o
})

export const isMuuLukioOpinto2015 = (a: any): a is MuuLukioOpinto2015 =>
  a?.$class === 'MuuLukioOpinto2015'
