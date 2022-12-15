import {
  MuuPerusopetuksenLisäopetuksenSuoritus,
  isMuuPerusopetuksenLisäopetuksenSuoritus
} from './MuuPerusopetuksenLisaopetuksenSuoritus'
import {
  PerusopetuksenLisäopetuksenOppiaineenSuoritus,
  isPerusopetuksenLisäopetuksenOppiaineenSuoritus
} from './PerusopetuksenLisaopetuksenOppiaineenSuoritus'
import {
  PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus,
  isPerusopetuksenLisäopetuksenToiminta_AlueenSuoritus
} from './PerusopetuksenLisaopetuksenToimintaAlueenSuoritus'

/**
 * PerusopetuksenLisäopetuksenAlisuoritus
 *
 * @see `fi.oph.koski.schema.PerusopetuksenLisäopetuksenAlisuoritus`
 */
export type PerusopetuksenLisäopetuksenAlisuoritus =
  | MuuPerusopetuksenLisäopetuksenSuoritus
  | PerusopetuksenLisäopetuksenOppiaineenSuoritus
  | PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus

export const isPerusopetuksenLisäopetuksenAlisuoritus = (
  a: any
): a is PerusopetuksenLisäopetuksenAlisuoritus =>
  isMuuPerusopetuksenLisäopetuksenSuoritus(a) ||
  isPerusopetuksenLisäopetuksenOppiaineenSuoritus(a) ||
  isPerusopetuksenLisäopetuksenToiminta_AlueenSuoritus(a)
