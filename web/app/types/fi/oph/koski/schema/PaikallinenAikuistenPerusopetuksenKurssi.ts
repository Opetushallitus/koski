import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusVuosiviikkotunneissaTaiKursseissa } from './LaajuusVuosiviikkotunneissaTaiKursseissa'

/**
 * PaikallinenAikuistenPerusopetuksenKurssi
 *
 * @see `fi.oph.koski.schema.PaikallinenAikuistenPerusopetuksenKurssi`
 */
export type PaikallinenAikuistenPerusopetuksenKurssi = {
  $class: 'fi.oph.koski.schema.PaikallinenAikuistenPerusopetuksenKurssi'
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
}

export const PaikallinenAikuistenPerusopetuksenKurssi = (o: {
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
}): PaikallinenAikuistenPerusopetuksenKurssi => ({
  $class: 'fi.oph.koski.schema.PaikallinenAikuistenPerusopetuksenKurssi',
  ...o
})

PaikallinenAikuistenPerusopetuksenKurssi.className =
  'fi.oph.koski.schema.PaikallinenAikuistenPerusopetuksenKurssi' as const

export const isPaikallinenAikuistenPerusopetuksenKurssi = (
  a: any
): a is PaikallinenAikuistenPerusopetuksenKurssi =>
  a?.$class === 'fi.oph.koski.schema.PaikallinenAikuistenPerusopetuksenKurssi'
