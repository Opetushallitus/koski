import { HenkilönOpiskeluoikeusVersiot } from '../types/fi/oph/koski/oppija/HenkilonOpiskeluoikeusVersiot'
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
