import { HenkilönOpiskeluoikeusVersiot } from '../types/fi/oph/koski/oppija/HenkilonOpiskeluoikeusVersiot'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { isKorkeakoulunOpiskeluoikeus } from '../types/fi/oph/koski/schema/KorkeakoulunOpiskeluoikeus'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'
import { isYlioppilastutkinnonOpiskeluoikeus } from '../types/fi/oph/koski/schema/YlioppilastutkinnonOpiskeluoikeus'
import { fetchOpiskeluoikeus } from './koskiApi'
import { isRight } from 'fp-ts/Either'

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

export const mergeOpiskeluoikeusVersionumeroAndRefetch =
  <T extends Opiskeluoikeus>(update: HenkilönOpiskeluoikeusVersiot) =>
  async (oo: T): Promise<T> => {
    const oid = (oo as any)?.oid as string | undefined
    const versio = oid && update.opiskeluoikeudet.find((o) => o.oid === oid)

    // Haetaan opiskeluoikeus uudestaan jotta saadaan backendista täydennetyt kentät
    const refetchedOo =
      oid && versio
        ? await fetchOpiskeluoikeus(oid, versio.versionumero)
        : undefined

    if (refetchedOo && isRight(refetchedOo)) {
      return refetchedOo.right.data as T
    }

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
