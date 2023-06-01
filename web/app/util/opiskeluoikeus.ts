import { HenkilönOpiskeluoikeusVersiot } from '../types/fi/oph/koski/oppija/HenkilonOpiskeluoikeusVersiot'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { isKorkeakoulunOpiskeluoikeus } from '../types/fi/oph/koski/schema/KorkeakoulunOpiskeluoikeus'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'
import { isYlioppilastutkinnonOpiskeluoikeus } from '../types/fi/oph/koski/schema/YlioppilastutkinnonOpiskeluoikeus'

export type PäätasonSuoritusOf<T extends Opiskeluoikeus> = T['suoritukset'][0]

export const mergeOpiskeluoikeusVersionumero =
  <T extends Opiskeluoikeus>(update: HenkilönOpiskeluoikeusVersiot) =>
  (oo: T): T => {
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

export const isValmistuvaTerminaalitila = (tila: Koodistokoodiviite): boolean =>
  ['valmistunut', 'hyvaksytystisuoritettu'].includes(tila.koodiarvo)

export const getOpiskeluoikeusOid = (oo: Opiskeluoikeus): string | undefined =>
  isYlioppilastutkinnonOpiskeluoikeus(oo) ? undefined : oo.oid

export const getVersionumero = (oo: Opiskeluoikeus): number | undefined =>
  isKorkeakoulunOpiskeluoikeus(oo) ? undefined : oo.versionumero
