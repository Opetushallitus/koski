import { formatISODate } from '../date/date.js'

export const makeSuoritus = (
  oppilaitos,
  koulutusmoduulinTunniste,
  curriculum,
  alkamispäivä
) => {
  if (!oppilaitos || !koulutusmoduulinTunniste || !curriculum) return null

  return {
    koulutusmoduuli: { tunniste: koulutusmoduulinTunniste, curriculum },
    toimipiste: oppilaitos,
    alkamispäivä: alkamispäivä ? formatISODate(alkamispäivä) : undefined,
    tyyppi: {
      koodiarvo: suoritusTyyppi(koulutusmoduulinTunniste),
      koodistoUri: 'suorituksentyyppi'
    }
  }
}

/**
 * European School of Helsinki -opiskeluoikeudessa käytettyjen koulutusmoduulin tunnisteiden suorituksen tyypit
 */
export const eshSuorituksenTyyppi = {
  nursery: 'europeanschoolofhelsinkivuosiluokkanursery',
  primary: 'europeanschoolofhelsinkivuosiluokkaprimary',
  secondaryLower: 'europeanschoolofhelsinkivuosiluokkasecondarylower',
  secondaryUpper: 'europeanschoolofhelsinkivuosiluokkasecondaryupper',
  ebtutkinto: 'ebtutkinto'
}

/**
 * European School of Helsinki -opiskeluoikeudessa käytettyjen suoritusten class:t
 */
export const eshSuorituksenClass = {
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
  secondaryUppers7alaosasuoritus: 's7oppiaineenalaosasuoritus',
  primaryOsasuoritus: 'primaryosasuoritus'
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
 * Palauttaa ESH-opiskeluoikeuden suoritustyypin sen koulutusmoduulin tunnisteen koodiarvon perusteella
 * @param {string} koulutusmoduulinTunniste Luokka-asteen koodiarvo
 * @returns {string} Suoritustyyppi
 */
export const suoritusTyyppi = (koulutusmoduulinTunniste) => {
  const nursery = ['N1', 'N2']
  const primary = ['P1', 'P2', 'P3', 'P4', 'P5']
  const secondaryLower = ['S1', 'S2', 'S3', 'S4', 'S5']
  const secondaryUpper = ['S6', 'S7']
  if (nursery.includes(koulutusmoduulinTunniste.koodiarvo)) {
    return eshSuorituksenTyyppi.nursery
  }
  if (primary.includes(koulutusmoduulinTunniste.koodiarvo)) {
    return eshSuorituksenTyyppi.primary
  }
  if (secondaryLower.includes(koulutusmoduulinTunniste.koodiarvo)) {
    return eshSuorituksenTyyppi.secondaryLower
  }
  if (secondaryUpper.includes(koulutusmoduulinTunniste.koodiarvo)) {
    return eshSuorituksenTyyppi.secondaryUpper
  }
  if (
    koulutusmoduulinTunniste.koodistoUri === 'koulutus' &&
    koulutusmoduulinTunniste.koodiarvo == '301104'
  ) {
    return eshSuorituksenTyyppi.ebtutkinto
  }

  throw new Error(`suoritusTyyppi not found for ${koulutusmoduulinTunniste}`)
}

export const suoritusPrototypeKey = (suorituksenTyyppi) => {
  switch (suorituksenTyyppi) {
    case eshSuorituksenTyyppi.nursery:
      return eshSuorituksenClass.nursery
    case eshSuorituksenTyyppi.primary:
      return eshSuorituksenClass.primary
    case eshSuorituksenTyyppi.secondaryLower:
      return eshSuorituksenClass.secondaryLowerVuosiluokka
    case eshSuorituksenTyyppi.secondaryUpper:
      return eshSuorituksenClass.secondaryUpperVuosiluokka
    case eshSuorituksenTyyppi.ebtutkinto:
      return eshSuorituksenClass.ebtutkinto
    default:
      throw new Error(`suoritusProtypeKey not found for ${suorituksenTyyppi}`)
  }
}
