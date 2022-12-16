import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissaTaiKursseissa } from './LaajuusVuosiviikkotunneissaTaiKursseissa'

/**
 * ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017
 *
 * @see `fi.oph.koski.schema.ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017`
 */
export type ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017 = {
  $class: 'fi.oph.koski.schema.ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017'
  tunniste: Koodistokoodiviite<
    'aikuistenperusopetuksenalkuvaiheenkurssit2017',
    string
  >
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
}

export const ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017 =
  (o: {
    tunniste: Koodistokoodiviite<
      'aikuistenperusopetuksenalkuvaiheenkurssit2017',
      string
    >
    laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  }): ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017 => ({
    $class:
      'fi.oph.koski.schema.ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017',
    ...o
  })

export const isValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017 = (
  a: any
): a is ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017 =>
  a?.$class === 'ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017'
