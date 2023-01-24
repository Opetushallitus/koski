import { suorituksenTyyppi } from '../suoritus/Suoritus'
import { Deprecated_Koodistokoodiviite } from '../types/common'

export const jotpaSallitutRahoituskoodiarvot = ['14', '15']

export const isJotpaRahoitteinen = (
  opiskeluoikeudenTyyppi: Deprecated_Koodistokoodiviite,
  suorituksenTyyppi?: Deprecated_Koodistokoodiviite
) =>
  isJotpaRahoitteinenKoodiarvo(
    opiskeluoikeudenTyyppi.koodiarvo,
    suorituksenTyyppi?.koodiarvo
  )

export const isJotpaRahoitteinenKoodiarvo = (
  opiskeluoikeudenTyyppiKoodiarvo: string,
  suoritustyyppiKoodiarvo?: string
) =>
  opiskeluoikeudenTyyppiKoodiarvo === 'muukuinsaanneltykoulutus' ||
  (opiskeluoikeudenTyyppiKoodiarvo === 'vapaansivistystyonkoulutus' &&
    suoritustyyppiKoodiarvo === 'vstjotpakoulutus')
