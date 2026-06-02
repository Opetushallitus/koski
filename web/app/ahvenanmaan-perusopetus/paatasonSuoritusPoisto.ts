import type { AhvenanmaanPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOpiskeluoikeus'
import type { AhvenanmaanPerusopetuksenPäätasonSuoritus } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenPaatasonSuoritus'

export const poistettavaPäätasonSuoritus = (
  opiskeluoikeus: AhvenanmaanPerusopetuksenOpiskeluoikeus,
  suoritusIndex: number
): AhvenanmaanPerusopetuksenPäätasonSuoritus | undefined =>
  opiskeluoikeus.suoritukset[suoritusIndex]
