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
import { EiTiedossaOppiaine, isEiTiedossaOppiaine } from './EiTiedossaOppiaine'
import {
  MuuAikuistenPerusopetuksenOppiaine,
  isMuuAikuistenPerusopetuksenOppiaine
} from './MuuAikuistenPerusopetuksenOppiaine'

/**
 * AikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine`
 */
export type AikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine =
  | AikuistenPerusopetuksenPaikallinenOppiaine
  | AikuistenPerusopetuksenUskonto
  | AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli
  | AikuistenPerusopetuksenÄidinkieliJaKirjallisuus
  | EiTiedossaOppiaine
  | MuuAikuistenPerusopetuksenOppiaine

export const isAikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine = (
  a: any
): a is AikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine =>
  isAikuistenPerusopetuksenPaikallinenOppiaine(a) ||
  isAikuistenPerusopetuksenUskonto(a) ||
  isAikuistenPerusopetuksenVierasTaiToinenKotimainenKieli(a) ||
  isAikuistenPerusopetuksenÄidinkieliJaKirjallisuus(a) ||
  isEiTiedossaOppiaine(a) ||
  isMuuAikuistenPerusopetuksenOppiaine(a)
