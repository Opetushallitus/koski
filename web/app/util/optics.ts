import * as A from 'fp-ts/Array'
import * as $ from 'optics-ts'
import { FormOptic, modifyValue } from '../components-v2/forms/FormModel'
import { localize, t } from '../i18n/i18n'
import { Finnish } from '../types/fi/oph/koski/schema/Finnish'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'
import { PäätasonSuoritusOf } from './opiskeluoikeus'
import { parasArviointi, parasArviointiIndex } from './arvioinnit'
import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'
import { deleteAt, updateAt } from './array'

/**
 * Yksilöllinen merkkiarvo, jota ei voi esiintyä lomakedatassa.
 */
const probeMarker = Symbol('parsePath')

/**
 * Kirjoittaa merkkiarvon optiikan osoittamaan paikkaan. Palauttaa undefined, jos optiikka on viallinen.
 */
const writeProbe = <S, A>(optic: FormOptic<S, A>, s: S): unknown => {
  try {
    return modifyValue(optic as unknown as FormOptic<S, unknown>)(
      () => probeMarker
    )(s)
  } catch {
    console.error('An invalid optic detected')
    return undefined
  }
}

/**
 * Etsii merkkiarvon sijainnit muokatusta datasta. Optics-ts jakaa koskemattomat haarat
 * rakenteellisesti alkuperäisen datan kanssa, joten viittausvertailu rajaa haun optiikan
 * koskettamaan haaraan koko datarakenteen läpikäynnin sijaan.
 */
const findProbe = (
  original: unknown,
  probed: unknown,
  path: string[],
  found: string[][]
): string[][] => {
  if (probed === probeMarker) {
    found.push(path)
  } else if (
    probed !== original &&
    probed !== null &&
    typeof probed === 'object'
  ) {
    const originalObj =
      original !== null && typeof original === 'object'
        ? (original as Record<string, unknown>)
        : undefined
    Object.entries(probed).forEach(([key, value]) => {
      findProbe(originalObj?.[key], value, [...path, key], found)
    })
  }
  return found
}

const commonPrefix = (a: string[], b: string[]): string[] => {
  const shared: string[] = []
  const len = Math.min(a.length, b.length)
  for (let i = 0; i < len && a[i] === b[i]; i++) {
    shared.push(a[i])
  }
  return shared
}

/**
 * Palauttaa polun, johon optiikka osoittaa annetussa datassa. Polku on muotoa esimerkiksi "lapset.0.nimi".
 *
 * Polku selvitetään kirjoittamalla optiikan läpi yksilöllinen merkkiarvo ja etsimällä se tuloksesta.
 * Näin polku löytyy myös silloin, kun kentän arvo on tyhjä, undefined tai avain puuttuu objektista
 * kokonaan — eli juuri silloin, kun kentän validointivirheet pitäisi näyttää. (Aiempi toteutus etsi
 * kentän *arvoa* datasta, jolloin tyhjä arvo jäi aina osoitteettomaksi.)
 *
 * Jos optiikka levittää arvon useaan paikkaan (esim. allLanguages kirjoittaa fi/sv/en), palautetaan
 * osumien yhteinen alkuosa, jolloin polku osoittaa yhteiseen isäntäobjektiin.
 *
 * Paluuarvo on undefined, jos optiikka ei osoita mihinkään annetussa datassa (esim. .optional()
 * puuttuvan arvon päällä tai .at() taulukon ulkopuolella) tai jos optiikka osoittaa datan juureen.
 *
 * @param optic
 * @param s
 * @returns
 */
export const parsePath = <S, A>(
  optic: FormOptic<S, A>,
  s: S
): string | undefined => {
  const probed = writeProbe(optic, s)
  if (probed === undefined) {
    return undefined
  }
  const paths = findProbe(s, probed, [], [])
  if (A.isEmpty(paths)) {
    return undefined
  }
  const path = paths.reduce(commonPrefix)
  return A.isEmpty(path) ? undefined : path.join('.')
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
const noMatch = Symbol('noMatch')
export const lastElement = <T>() =>
  $.optic_<T[]>()
    .lens(
      (as): T | typeof noMatch => {
        return as.length === 0 ? noMatch : as[as.length - 1]
      },
      (as, v): T[] => {
        return v === noMatch ? as : [...as.slice(0, -1), v]
      }
    )
    .iso(
      (a) => (a === noMatch ? undefined : a),
      (b) => (b === undefined ? noMatch : b)
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
