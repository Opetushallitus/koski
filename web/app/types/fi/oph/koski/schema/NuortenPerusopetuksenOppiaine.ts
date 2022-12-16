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
 * NuortenPerusopetuksenOppiaine
 *
 * @see `fi.oph.koski.schema.NuortenPerusopetuksenOppiaine`
 */
export type NuortenPerusopetuksenOppiaine =
  | MuuNuortenPerusopetuksenOppiaine
  | NuortenPerusopetuksenPaikallinenOppiaine
  | NuortenPerusopetuksenUskonto
  | NuortenPerusopetuksenVierasTaiToinenKotimainenKieli
  | NuortenPerusopetuksenÄidinkieliJaKirjallisuus

export const isNuortenPerusopetuksenOppiaine = (
  a: any
): a is NuortenPerusopetuksenOppiaine =>
  isMuuNuortenPerusopetuksenOppiaine(a) ||
  isNuortenPerusopetuksenPaikallinenOppiaine(a) ||
  isNuortenPerusopetuksenUskonto(a) ||
  isNuortenPerusopetuksenVierasTaiToinenKotimainenKieli(a) ||
  isNuortenPerusopetuksenÄidinkieliJaKirjallisuus(a)
