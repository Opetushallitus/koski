import { suorituksenTyyppi } from '../suoritus/Suoritus'
import { Koodistokoodiviite } from '../types/common'

export const jotpaSallitutRahoituskoodiarvot = ['14', '15']

export const isJotpaRahoitteinen = (
  opiskeluoikeudenTyyppi: Koodistokoodiviite,
  suorituksenTyyppi?: Koodistokoodiviite
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
