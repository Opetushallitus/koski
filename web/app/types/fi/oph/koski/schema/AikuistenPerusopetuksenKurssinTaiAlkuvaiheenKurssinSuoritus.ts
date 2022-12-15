import {
  AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus,
  isAikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus
} from './AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus'
import {
  AikuistenPerusopetuksenKurssinSuoritus,
  isAikuistenPerusopetuksenKurssinSuoritus
} from './AikuistenPerusopetuksenKurssinSuoritus'

/**
 * AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus`
 */
export type AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus =
  | AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus
  | AikuistenPerusopetuksenKurssinSuoritus

export const isAikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus = (
  a: any
): a is AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus =>
  isAikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus(a) ||
  isAikuistenPerusopetuksenKurssinSuoritus(a)
