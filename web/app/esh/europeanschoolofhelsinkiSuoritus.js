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
    return 'europeanschoolofhelsinkivuosiluokkanursery'
  }
  if (primary.includes(luokkaaste.koodiarvo)) {
    return 'europeanschoolofhelsinkivuosiluokkaprimary'
  }
  if (secondaryLower.includes(luokkaaste.koodiarvo)) {
    return 'europeanschoolofhelsinkivuosiluokkasecondarylower'
  }
  if (secondaryUpper.includes(luokkaaste.koodiarvo)) {
    return 'europeanschoolofhelsinkivuosiluokkasecondaryupper'
  }
  return ''
}

const eshSuoritusPrototypes = {
  europeanschoolofhelsinkivuosiluokkanursery: 'nurseryvuosiluokansuoritus',
  europeanschoolofhelsinkivuosiluokkaprimary: 'primaryvuosiluokansuoritus',
  europeanschoolofhelsinkivuosiluokkasecondarylower:
    'secondarylowervuosiluokansuoritus',
  europeanschoolofhelsinkivuosiluokkasecondaryupper:
    'secondaryuppervuosiluokansuoritus'
}

export const suoritusPrototypeKey = (type) => eshSuoritusPrototypes[type]
