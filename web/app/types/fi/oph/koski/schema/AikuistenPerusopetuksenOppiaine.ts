import {
  AikuistenPerusopetuksenPaikallinenOppiaine,
  isAikuistenPerusopetuksenPaikallinenOppiaine
} from './AikuistenPerusopetuksenPaikallinenOppiaine'
import {
  AikuistenPerusopetuksenUskonto,
  isAikuistenPerusopetuksenUskonto
} from './AikuistenPerusopetuksenUskonto'
import {
  AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli,
  isAikuistenPerusopetuksenVierasTaiToinenKotimainenKieli
} from './AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli'
import {
  AikuistenPerusopetuksenÄidinkieliJaKirjallisuus,
  isAikuistenPerusopetuksenÄidinkieliJaKirjallisuus
} from './AikuistenPerusopetuksenAidinkieliJaKirjallisuus'
import {
  MuuAikuistenPerusopetuksenOppiaine,
  isMuuAikuistenPerusopetuksenOppiaine
} from './MuuAikuistenPerusopetuksenOppiaine'

/**
 * AikuistenPerusopetuksenOppiaine
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenOppiaine`
 */
export type AikuistenPerusopetuksenOppiaine =
  | AikuistenPerusopetuksenPaikallinenOppiaine
  | AikuistenPerusopetuksenUskonto
  | AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli
  | AikuistenPerusopetuksenÄidinkieliJaKirjallisuus
  | MuuAikuistenPerusopetuksenOppiaine

export const isAikuistenPerusopetuksenOppiaine = (
  a: any
): a is AikuistenPerusopetuksenOppiaine =>
  isAikuistenPerusopetuksenPaikallinenOppiaine(a) ||
  isAikuistenPerusopetuksenUskonto(a) ||
  isAikuistenPerusopetuksenVierasTaiToinenKotimainenKieli(a) ||
  isAikuistenPerusopetuksenÄidinkieliJaKirjallisuus(a) ||
  isMuuAikuistenPerusopetuksenOppiaine(a)
