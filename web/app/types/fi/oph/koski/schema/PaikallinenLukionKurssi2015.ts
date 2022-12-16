import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusKursseissa } from './LaajuusKursseissa'
import { LocalizedString } from './LocalizedString'
import { Koodistokoodiviite } from './Koodistokoodiviite'

/**
 * Paikallisen lukion/IB-lukion kurssin tunnistetiedot
 *
 * @see `fi.oph.koski.schema.PaikallinenLukionKurssi2015`
 */
export type PaikallinenLukionKurssi2015 = {
  $class: 'fi.oph.koski.schema.PaikallinenLukionKurssi2015'
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKursseissa
  kuvaus: LocalizedString
  kurssinTyyppi: Koodistokoodiviite<'lukionkurssintyyppi', string>
}

export const PaikallinenLukionKurssi2015 = (o: {
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKursseissa
  kuvaus: LocalizedString
  kurssinTyyppi: Koodistokoodiviite<'lukionkurssintyyppi', string>
}): PaikallinenLukionKurssi2015 => ({
  $class: 'fi.oph.koski.schema.PaikallinenLukionKurssi2015',
  ...o
})

export const isPaikallinenLukionKurssi2015 = (
  a: any
): a is PaikallinenLukionKurssi2015 =>
  a?.$class === 'PaikallinenLukionKurssi2015'
