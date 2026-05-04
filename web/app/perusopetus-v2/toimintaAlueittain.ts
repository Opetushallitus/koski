import { PerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/PerusopetuksenOpiskeluoikeus'

export const isToimintaAlueittainOpiskelu = (
  opiskeluoikeus: PerusopetuksenOpiskeluoikeus
): boolean => {
  const lisätiedot = opiskeluoikeus.lisätiedot
  if (!lisätiedot) return false

  if (lisätiedot.erityisenTuenPäätös?.opiskeleeToimintaAlueittain) {
    return true
  }

  if (
    lisätiedot.erityisenTuenPäätökset?.some(
      (p) => p.opiskeleeToimintaAlueittain
    )
  ) {
    return true
  }

  return Boolean(
    lisätiedot.toimintaAlueittainOpiskelu &&
    lisätiedot.toimintaAlueittainOpiskelu.length > 0
  )
}
