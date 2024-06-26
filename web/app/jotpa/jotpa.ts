import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'

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

export const isJotpaRahoituksenKoodistoviite = (
  opintojenRahoitus: Koodistokoodiviite<'opintojenrahoitus'>
): boolean => ['14', '15'].includes(opintojenRahoitus.koodiarvo)
