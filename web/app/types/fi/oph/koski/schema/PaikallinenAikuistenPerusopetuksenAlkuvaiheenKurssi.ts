import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusVuosiviikkotunneissaTaiKursseissa } from './LaajuusVuosiviikkotunneissaTaiKursseissa'

/**
 * PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi
 *
 * @see `fi.oph.koski.schema.PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi`
 */
export type PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi = {
  $class: 'fi.oph.koski.schema.PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi'
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
}

export const PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi = (o: {
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
}): PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi => ({
  $class:
    'fi.oph.koski.schema.PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi',
  ...o
})

PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi.className =
  'fi.oph.koski.schema.PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi' as const

export const isPaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi = (
  a: any
): a is PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi =>
  a?.$class ===
  'fi.oph.koski.schema.PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi'
