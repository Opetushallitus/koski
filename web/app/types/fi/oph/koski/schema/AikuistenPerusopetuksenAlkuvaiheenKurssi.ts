import {
  PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi,
  isPaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi
} from './PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi'
import {
  ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017,
  isValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017
} from './ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017'

/**
 * AikuistenPerusopetuksenAlkuvaiheenKurssi
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenKurssi`
 */
export type AikuistenPerusopetuksenAlkuvaiheenKurssi =
  | PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi
  | ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017

export const isAikuistenPerusopetuksenAlkuvaiheenKurssi = (
  a: any
): a is AikuistenPerusopetuksenAlkuvaiheenKurssi =>
  isPaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi(a) ||
  isValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017(a)
