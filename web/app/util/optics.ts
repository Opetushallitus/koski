import * as A from 'fp-ts/Array'
import * as $ from 'optics-ts'
import { useMemo } from 'react'
import { FormOptic, getValue } from '../components-v2/forms/FormModel'
import { localize, t } from '../i18n/i18n'
import { Finnish } from '../types/fi/oph/koski/schema/Finnish'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'
import { PäätasonSuoritusOf } from './opiskeluoikeus'
import { parasArviointi, parasArviointiIndex } from './arvioinnit'
import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'
import { deleteAt, updateAt } from './array'

/**
 * Palauttaa polun, johon optiikka osoittaa annetussa datassa. Polku on muotoa esimerkiksi "lapset.0.nimi.fi".
 * Paluuarvo on undefined, jos optiikka ei osoita mihinkään annetussa datassa. Ainoastaan objekteihin tarkentuvat
 * optiikat ovat sallittuja, koska primitiivien yksilöllisyyttä ei voida varmentaa.
 *
 * @param optic
 * @param s
 * @returns
 */
export const parsePath = <S, A extends object>(
  optic: FormOptic<S, A>,
  s: S
): string | undefined => {
  const walk = (haystack: any, needle: any): string[] | undefined => {
    if (haystack === needle) {
      return []
    }
    if (haystack === null || haystack === undefined) {
      return undefined
    }
    if (typeof haystack === 'object') {
      const entries = Object.entries(haystack).map(([key, value]) => ({
        // TODO: perffioptimointi
        key,
        path: walk(value, needle)
      }))
      const entry = entries.find((e) => e.path)
      return entry?.path ? [entry.key, ...entry.path] : undefined
    }
    return undefined
  }

  const needle = getValue(optic)(s)
  const path = needle && walk(s, needle)
  return path ? path.join('.') : undefined
}

/**
 * Isomorfismi jolla voi viitata LocalizedStringin kaikkiin kielikenttiin
 */
export const allLanguages = $.optic_<LocalizedString>().iso(
  (localized: LocalizedString) =>
    (localized as any).fi || (localized as any).sv || localized.en,
  (str: string) =>
    Finnish({
      fi: str,
      sv: str,
      en: str
    })
)

export const currentLanguage = $.optic_<LocalizedString>().iso(t, localize)

/**
 * Linssi jolla voi viitata taulukon viimeiseen alkioon
 */
export const lastElement = <T>() =>
  $.optic_<T[]>()
    .lens(
      (as): T | undefined => as[as.length - 1],
      (as, v) => (v === undefined ? as.slice(0, -1) : [...as.slice(0, -1), v])
    )
    .optional()

/**
 * Linssi, jolla voi viitata parhaaseen arvosanaan
 */
export const addToArviointi = <T extends Arviointi>(
  arviointi: T[],
  uusi: T
): T[] => (arviointi.length < 2 ? [uusi] : [...arviointi, uusi])

export const parasArviointiElement = <T extends Arviointi>() =>
  $.optic_<T[]>()
    .lens(
      (as): T | undefined => parasArviointi(as),
      (as, v) => (v === undefined ? as.slice(0, -1) : addToArviointi(as, v)) // uusi arviointi laitetaan silti edelleen listan perään
    )
    .optional()

/**
 * Opiskeluoikeuden päätason suoritus
 */
export const päätasonSuoritusPath = <T extends Opiskeluoikeus = Opiskeluoikeus>(
  index = 0
): FormOptic<T, PäätasonSuoritusOf<T>> =>
  $.optic_<T>().prop('suoritukset').at(index) as any
