import { EiTiedossaOppiaine, isEiTiedossaOppiaine } from './EiTiedossaOppiaine'
import {
  MuuNuortenPerusopetuksenOppiaine,
  isMuuNuortenPerusopetuksenOppiaine
} from './MuuNuortenPerusopetuksenOppiaine'
import {
  NuortenPerusopetuksenPaikallinenOppiaine,
  isNuortenPerusopetuksenPaikallinenOppiaine
} from './NuortenPerusopetuksenPaikallinenOppiaine'
import {
  NuortenPerusopetuksenUskonto,
  isNuortenPerusopetuksenUskonto
} from './NuortenPerusopetuksenUskonto'
import {
  NuortenPerusopetuksenVierasTaiToinenKotimainenKieli,
  isNuortenPerusopetuksenVierasTaiToinenKotimainenKieli
} from './NuortenPerusopetuksenVierasTaiToinenKotimainenKieli'
import {
  NuortenPerusopetuksenÄidinkieliJaKirjallisuus,
  isNuortenPerusopetuksenÄidinkieliJaKirjallisuus
} from './NuortenPerusopetuksenAidinkieliJaKirjallisuus'

/**
 * NuortenPerusopetuksenOppiainenTaiEiTiedossaOppiaine
 *
 * @see `fi.oph.koski.schema.NuortenPerusopetuksenOppiainenTaiEiTiedossaOppiaine`
 */
export type NuortenPerusopetuksenOppiainenTaiEiTiedossaOppiaine =
  | EiTiedossaOppiaine
  | MuuNuortenPerusopetuksenOppiaine
  | NuortenPerusopetuksenPaikallinenOppiaine
  | NuortenPerusopetuksenUskonto
  | NuortenPerusopetuksenVierasTaiToinenKotimainenKieli
  | NuortenPerusopetuksenÄidinkieliJaKirjallisuus

export const isNuortenPerusopetuksenOppiainenTaiEiTiedossaOppiaine = (
  a: any
): a is NuortenPerusopetuksenOppiainenTaiEiTiedossaOppiaine =>
  isEiTiedossaOppiaine(a) ||
  isMuuNuortenPerusopetuksenOppiaine(a) ||
  isNuortenPerusopetuksenPaikallinenOppiaine(a) ||
  isNuortenPerusopetuksenUskonto(a) ||
  isNuortenPerusopetuksenVierasTaiToinenKotimainenKieli(a) ||
  isNuortenPerusopetuksenÄidinkieliJaKirjallisuus(a)
