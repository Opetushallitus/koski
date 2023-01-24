import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusKursseissa } from './LaajuusKursseissa'

/**
 * Valtakunnallisen lukion/IB-lukion kurssin tunnistetiedot
 *
 * @see `fi.oph.koski.schema.ValtakunnallinenLukionKurssi2015`
 */
export type ValtakunnallinenLukionKurssi2015 = {
  $class: 'fi.oph.koski.schema.ValtakunnallinenLukionKurssi2015'
  tunniste: Koodistokoodiviite<
    | 'lukionkurssit'
    | 'lukionkurssitops2004aikuiset'
    | 'lukionkurssitops2003nuoret',
    string
  >
  laajuus?: LaajuusKursseissa
  kurssinTyyppi: Koodistokoodiviite<'lukionkurssintyyppi', string>
}

export const ValtakunnallinenLukionKurssi2015 = (o: {
  tunniste: Koodistokoodiviite<
    | 'lukionkurssit'
    | 'lukionkurssitops2004aikuiset'
    | 'lukionkurssitops2003nuoret',
    string
  >
  laajuus?: LaajuusKursseissa
  kurssinTyyppi: Koodistokoodiviite<'lukionkurssintyyppi', string>
}): ValtakunnallinenLukionKurssi2015 => ({
  $class: 'fi.oph.koski.schema.ValtakunnallinenLukionKurssi2015',
  ...o
})

ValtakunnallinenLukionKurssi2015.className =
  'fi.oph.koski.schema.ValtakunnallinenLukionKurssi2015' as const

export const isValtakunnallinenLukionKurssi2015 = (
  a: any
): a is ValtakunnallinenLukionKurssi2015 =>
  a?.$class === 'fi.oph.koski.schema.ValtakunnallinenLukionKurssi2015'
