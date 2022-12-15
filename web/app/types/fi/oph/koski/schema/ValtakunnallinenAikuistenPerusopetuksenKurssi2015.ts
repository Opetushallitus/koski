import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissaTaiKursseissa } from './LaajuusVuosiviikkotunneissaTaiKursseissa'

/**
 * ValtakunnallinenAikuistenPerusopetuksenKurssi2015
 *
 * @see `fi.oph.koski.schema.ValtakunnallinenAikuistenPerusopetuksenKurssi2015`
 */
export type ValtakunnallinenAikuistenPerusopetuksenKurssi2015 = {
  $class: 'fi.oph.koski.schema.ValtakunnallinenAikuistenPerusopetuksenKurssi2015'
  tunniste: Koodistokoodiviite<'aikuistenperusopetuksenkurssit2015', string>
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
}

export const ValtakunnallinenAikuistenPerusopetuksenKurssi2015 = (o: {
  tunniste: Koodistokoodiviite<'aikuistenperusopetuksenkurssit2015', string>
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
}): ValtakunnallinenAikuistenPerusopetuksenKurssi2015 => ({
  $class:
    'fi.oph.koski.schema.ValtakunnallinenAikuistenPerusopetuksenKurssi2015',
  ...o
})

export const isValtakunnallinenAikuistenPerusopetuksenKurssi2015 = (
  a: any
): a is ValtakunnallinenAikuistenPerusopetuksenKurssi2015 =>
  a?.$class === 'ValtakunnallinenAikuistenPerusopetuksenKurssi2015'
