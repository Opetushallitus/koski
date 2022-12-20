import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissaTaiKursseissa } from './LaajuusVuosiviikkotunneissaTaiKursseissa'

/**
 * ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017
 *
 * @see `fi.oph.koski.schema.ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017`
 */
export type ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017 = {
  $class: 'fi.oph.koski.schema.ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017'
  tunniste: Koodistokoodiviite<
    'aikuistenperusopetuksenpaattovaiheenkurssit2017',
    string
  >
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
}

export const ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017 =
  (o: {
    tunniste: Koodistokoodiviite<
      'aikuistenperusopetuksenpaattovaiheenkurssit2017',
      string
    >
    laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  }): ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017 => ({
    $class:
      'fi.oph.koski.schema.ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017',
    ...o
  })

export const isValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017 =
  (
    a: any
  ): a is ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017 =>
    a?.$class ===
    'fi.oph.koski.schema.ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017'
