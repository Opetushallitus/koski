import { formatISODate } from '../date/date.js'

export const makeSuoritus = (
  oppilaitos,
  luokkaaste,
  curriculum,
  alkamispäivä
) => {
  if (!oppilaitos || !luokkaaste || !curriculum) return null

  return {
    koulutusmoduuli: { tunniste: luokkaaste, curriculum },
    toimipiste: oppilaitos,
    alkamispäivä: formatISODate(alkamispäivä),
    tyyppi: {
      koodiarvo: suoritusTyyppi(luokkaaste),
      koodistoUri: 'suorituksentyyppi'
    }
  }
}

/**
 * European School of Helsinki -opiskeluoikeudessa käytettyjen luokka-asteiden suorituksen tyypit
 */
export const eshVuosiluokka = {
  nursery: 'europeanschoolofhelsinkivuosiluokkanursery',
  primary: 'europeanschoolofhelsinkivuosiluokkaprimary',
  secondaryLower: 'europeanschoolofhelsinkivuosiluokkasecondarylower',
  secondaryUpper: 'europeanschoolofhelsinkivuosiluokkasecondaryupper',
  ebtutkinto: 'ebtutkinnonsuoritus'
}

/**
 * European School of Helsinki -opiskeluoikeudessa käytettyjen suoritusten class:t
 */
export const eshSuoritus = {
  nursery: 'nurseryvuosiluokansuoritus',
  primary: 'primaryvuosiluokansuoritus',
  ebtutkinto: 'ebtutkinnonsuoritus',
  ebtutkintoOsasuoritus: 'ebtutkinnonosasuoritus',
  secondaryLowerVuosiluokka: 'secondarylowervuosiluokansuoritus',
  secondaryLowerOppiaine: 'secondaryloweroppiaineensuoritus',
  secondaryUpperOppiaine: 'secondaryupperoppiaineensuoritus',
  secondaryUpperVuosiluokka: 'secondaryuppervuosiluokansuoritus',
  secondaryUppers6: 'secondaryupperoppiaineensuorituss6',
  secondaryUppers7: 'secondaryupperoppiaineensuorituss7',
  secondaryUppers7alaosasuoritus: 's7oppiaineenalaosasuoritus'
}

export const eshSynteettisetKoodistot = {
  preliminary: 'esh/s7preliminarymark',
  final: 'esh/s7finalmark',
  numerical: 'esh/numericalmark'
}

const eshSynteettisetKoodistoUris = Object.values(eshSynteettisetKoodistot)

export const eshSynteettinenKoodiValidators = {
  preliminary: (
    val // 0, 10, tai luku siltä väliltä yhden desimaalin tarkkuudella
  ) =>
    val === '0' ||
    val === '10' ||
    /^0\.[1-9]$/.exec(val) !== null ||
    /^[1-9]\.\d$/.exec(val) !== null,
  // 10, tai luku 0.00 - 9.99 tasan kahden desimaalin tarkkuudella
  final: (val) => val === '10' || /^\d\.\d\d$/.exec(val) !== null,
  // 0, 10, tai luku siltä väliltä yhden desimaalin tarkkuudella, joka on 0 tai 5
  numerical: (val) =>
    val === '0' ||
    val === '0.5' ||
    val === '10' ||
    /^[1-9]\.[05]$/.exec(val) !== null
}

export const isEshSynteettinenKoodisto = (koodistoUriKoodiarvo) =>
  eshSynteettisetKoodistoUris.includes(koodistoUriKoodiarvo)

export const isEshPreliminaryArvosana = (koodistoUriKoodiarvo) =>
  koodistoUriKoodiarvo === eshSynteettisetKoodistot.preliminary
export const isEshFinalArvosana = (koodistoUriKoodiarvo) =>
  koodistoUriKoodiarvo === eshSynteettisetKoodistot.final
export const isEshNumericalArvosana = (koodistoUriKoodiarvo) =>
  koodistoUriKoodiarvo === eshSynteettisetKoodistot.numerical

export const isValidEshSynteettinenKoodiarvo = (koodistoUri, koodiarvo) => {
  if (!isEshSynteettinenKoodisto(koodistoUri)) {
    return false
  }
  switch (koodistoUri) {
    case eshSynteettisetKoodistot.preliminary:
      return eshSynteettinenKoodiValidators.preliminary(koodiarvo)
    case eshSynteettisetKoodistot.numerical:
      return eshSynteettinenKoodiValidators.numerical(koodiarvo)
    case eshSynteettisetKoodistot.final:
      return eshSynteettinenKoodiValidators.final(koodiarvo)
  }
}

/**
 * Palauttaa ESH-opiskeluoikeuden suoritustyypin sen luokka-asteen koodiarvon perusteella
 * @param {string} luokkaaste Luokka-asteen koodiarvo
 * @returns {string} Suoritustyyppi
 */
export const suoritusTyyppi = (luokkaaste) => {
  const nursery = ['N1', 'N2']
  const primary = ['P1', 'P2', 'P3', 'P4', 'P5']
  const secondaryLower = ['S1', 'S2', 'S3', 'S4', 'S5']
  const secondaryUpper = ['S6', 'S7']
  if (nursery.includes(luokkaaste.koodiarvo)) {
    return eshVuosiluokka.nursery
  }
  if (primary.includes(luokkaaste.koodiarvo)) {
    return eshVuosiluokka.primary
  }
  if (secondaryLower.includes(luokkaaste.koodiarvo)) {
    return eshVuosiluokka.secondaryLower
  }
  if (secondaryUpper.includes(luokkaaste.koodiarvo)) {
    return eshVuosiluokka.secondaryUpper
  }

  throw new Error(`suoritusTyyppi not found for ${luokkaaste}`)
}

export const suoritusPrototypeKey = (luokkaAste) => {
  switch (luokkaAste) {
    case eshVuosiluokka.nursery:
      return eshSuoritus.nursery
    case eshVuosiluokka.primary:
      return eshSuoritus.primary
    case eshVuosiluokka.secondaryLower:
      return eshSuoritus.secondaryLowerVuosiluokka
    case eshVuosiluokka.secondaryUpper:
      return eshSuoritus.secondaryUpperVuosiluokka
    default:
      throw new Error(`suoritusProtypeKey not found for ${luokkaAste}`)
  }
}
