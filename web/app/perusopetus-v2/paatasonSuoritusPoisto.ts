import type { PerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/PerusopetuksenOpiskeluoikeus'
import type { PerusopetuksenPäätasonSuoritus } from '../types/fi/oph/koski/schema/PerusopetuksenPaatasonSuoritus'

export const poistettavaPäätasonSuoritus = (
  opiskeluoikeus: PerusopetuksenOpiskeluoikeus,
  suoritusIndex: number
): PerusopetuksenPäätasonSuoritus | undefined =>
  opiskeluoikeus.suoritukset[suoritusIndex]
