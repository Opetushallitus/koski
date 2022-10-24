import { formatISODate } from '../date/date.js'

export const makeSuoritus = (
  oppilaitos,
  luokkaaste,
  curriculum,
  alkamispäivä,
  suorituskieli
) => {
  if (!oppilaitos || !luokkaaste || !curriculum || !suorituskieli) return null

  return {
    suorituskieli,
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
 * European School of Helsinki -opiskeluoikeudessa käytetyt luokka-asteet
 */
export const eshVuosiluokka = {
  nursery: 'europeanschoolofhelsinkivuosiluokkanursery',
  primary: 'europeanschoolofhelsinkivuosiluokkaprimary',
  secondaryLower: 'europeanschoolofhelsinkivuosiluokkasecondarylower',
  secondaryUpper: 'europeanschoolofhelsinkivuosiluokkasecondaryupper'
}

/**
 * European School of Helsinki -opiskeluoikeudessa käytetyt suoritustyypit
 */
export const eshSuoritus = {
  nursery: 'nurseryvuosiluokansuoritus',
  primary: 'primaryvuosiluokansuoritus',
  secondaryLower: 'secondarylowervuosiluokansuoritus',
  secondaryUpper: 'secondaryuppervuosiluokansuoritus'
}

export const eshSynteettisetKoodistot = {
  preliminary: 'esh/s7preliminarymark',
  final: 'esh/s7finalmark',
  numerical: 'esh/numericalmark'
}

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

export const isEshSynteettinenKoodisto = (modelData) =>
  modelData &&
  modelData.koodistoUri &&
  Object.values(eshSynteettisetKoodistot).includes(modelData.koodistoUri)

export const isEshPreliminaryArvosana = (modelData) =>
  modelData && modelData.koodistoUri === eshSynteettisetKoodistot.preliminary
export const isEshFinalArvosana = (modelData) =>
  modelData && modelData.koodistoUri === eshSynteettisetKoodistot.final
export const isEshNumericalArvosana = (modelData) =>
  modelData && modelData.koodistoUri === eshSynteettisetKoodistot.numerical

export const isValidEshSynteettinenKoodiarvo = (modelData) => {
  if (!isEshSynteettinenKoodisto(modelData)) {
    return false
  }
  switch (modelData.koodistoUri) {
    case eshSynteettisetKoodistot.preliminary:
      return (
        modelData.koodiarvo &&
        eshSynteettinenKoodiValidators.preliminary(modelData.koodiarvo)
      )
    case eshSynteettisetKoodistot.numerical:
      return (
        modelData.koodiarvo &&
        eshSynteettinenKoodiValidators.numerical(modelData.koodiarvo)
      )
    case eshSynteettisetKoodistot.final:
      return (
        modelData.koodiarvo &&
        eshSynteettinenKoodiValidators.final(modelData.koodiarvo)
      )
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
  return ''
}

export const suoritusPrototypeKey = (luokkaAste) => {
  switch (luokkaAste) {
    case eshVuosiluokka.nursery:
      return eshSuoritus.nursery
    case eshVuosiluokka.primary:
      return eshSuoritus.primary
    case eshVuosiluokka.secondaryLower:
      return eshSuoritus.secondaryLower
    case eshVuosiluokka.secondaryUpper:
      return eshSuoritus.secondaryUpper
  }
}
