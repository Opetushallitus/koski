import { HenkilönOpiskeluoikeusVersiot } from '../types/fi/oph/koski/oppija/HenkilonOpiskeluoikeusVersiot'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'

export const mergeOpiskeluoikeusVersionumero = <T extends Opiskeluoikeus>(
  oo: T,
  update: HenkilönOpiskeluoikeusVersiot
): T => {
  const oid = (oo as any)?.oid as string | undefined
  const versio = oid && update.opiskeluoikeudet.find((o) => o.oid === oid)
  return versio
    ? {
        ...oo,
        versionumero: versio.versionumero
      }
    : oo
}

export const isTerminaalitila = (tila: Koodistokoodiviite): boolean =>
  [
    'eronnut',
    'valmistunut',
    'peruutettu',
    'keskeytynyt',
    'hyvaksytystisuoritettu',
    'paattynyt',
    'mitatoity'
  ].includes(tila.koodiarvo)
