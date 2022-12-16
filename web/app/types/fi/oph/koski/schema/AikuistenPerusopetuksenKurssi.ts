import {
  PaikallinenAikuistenPerusopetuksenKurssi,
  isPaikallinenAikuistenPerusopetuksenKurssi
} from './PaikallinenAikuistenPerusopetuksenKurssi'
import {
  ValtakunnallinenAikuistenPerusopetuksenKurssi2015,
  isValtakunnallinenAikuistenPerusopetuksenKurssi2015
} from './ValtakunnallinenAikuistenPerusopetuksenKurssi2015'
import {
  ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017,
  isValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017
} from './ValtakunnallinenAikuistenPerusopetuksenPaattovaiheenKurssi2017'

/**
 * AikuistenPerusopetuksenKurssi
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenKurssi`
 */
export type AikuistenPerusopetuksenKurssi =
  | PaikallinenAikuistenPerusopetuksenKurssi
  | ValtakunnallinenAikuistenPerusopetuksenKurssi2015
  | ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017

export const isAikuistenPerusopetuksenKurssi = (
  a: any
): a is AikuistenPerusopetuksenKurssi =>
  isPaikallinenAikuistenPerusopetuksenKurssi(a) ||
  isValtakunnallinenAikuistenPerusopetuksenKurssi2015(a) ||
  isValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017(a)
