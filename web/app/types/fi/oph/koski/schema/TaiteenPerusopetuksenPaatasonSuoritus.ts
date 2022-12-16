import {
  TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus,
  isTaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus
} from './TaiteenPerusopetuksenLaajanOppimaaranPerusopintojenSuoritus'
import {
  TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus,
  isTaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus
} from './TaiteenPerusopetuksenLaajanOppimaaranSyventavienOpintojenSuoritus'
import {
  TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus,
  isTaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus
} from './TaiteenPerusopetuksenYleisenOppimaaranTeemaopintojenSuoritus'
import {
  TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus,
  isTaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus
} from './TaiteenPerusopetuksenYleisenOppimaaranYhteistenOpintojenSuoritus'

/**
 * TaiteenPerusopetuksenPäätasonSuoritus
 *
 * @see `fi.oph.koski.schema.TaiteenPerusopetuksenPäätasonSuoritus`
 */
export type TaiteenPerusopetuksenPäätasonSuoritus =
  | TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus
  | TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus
  | TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus
  | TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus

export const isTaiteenPerusopetuksenPäätasonSuoritus = (
  a: any
): a is TaiteenPerusopetuksenPäätasonSuoritus =>
  isTaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus(a) ||
  isTaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus(a) ||
  isTaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus(a) ||
  isTaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus(a)
