import { formatISODate } from '../date/date'

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

export const makeEBSuoritus = (
  oppilaitos,
  date, // date mukana vain bacon/React-yhteistoiminnan "korjaamiseksi": päivämäärän vaihto dialogilla sotkee muuten suoritus-atomin väärän tyyppiseksi
  curriculum
) => {
  if (!oppilaitos || !curriculum) return null

  return {
    koulutusmoduuli: {
      tunniste: { koodiarvo: '301104', koodistoUri: 'koulutus' },
      curriculum
    },
    toimipiste: oppilaitos,
    tyyppi: {
      koodiarvo: ebSuorituksenTyyppi.ebtutkinto,
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

export const ebSuorituksenTyyppi = {
  ebtutkinto: 'ebtutkinto'
}

/**
 * European School of Helsinki -opiskeluoikeudessa käytettyjen suoritusten class:t
 */
export const eshSuorituksenClass = {
  nursery: 'nurseryvuosiluokansuoritus',
  primary: 'primaryvuosiluokansuoritus',
  secondaryLowerVuosiluokka: 'secondarylowervuosiluokansuoritus',
  secondaryLowerOppiaine: 'secondaryloweroppiaineensuoritus',
  secondaryUpperOppiaine: 'secondaryupperoppiaineensuoritus',
  secondaryUpperVuosiluokka: 'secondaryuppervuosiluokansuoritus',
  secondaryUppers6: 'secondaryupperoppiaineensuorituss6',
  secondaryUppers7: 'secondaryupperoppiaineensuorituss7',
  secondaryUppers7alaosasuoritus: 's7oppiaineenalaosasuoritus',
  primaryOsasuoritus: 'primaryosasuoritus'
}

export const ebSuorituksenClass = {
  ebtutkinto: 'ebtutkinnonsuoritus',
  ebtutkintoOsasuoritus: 'ebtutkinnonosasuoritus'
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

  throw new Error(`suoritusTyyppi not found for ${koulutusmoduulinTunniste}`)
}

export const suoritusPrototypeKey = (
  opiskeluoikeudenTyyppi,
  suorituksenTyyppi
) => {
  if (opiskeluoikeudenTyyppi === 'europeanschoolofhelsinki') {
    switch (suorituksenTyyppi) {
      case eshSuorituksenTyyppi.nursery:
        return eshSuorituksenClass.nursery
      case eshSuorituksenTyyppi.primary:
        return eshSuorituksenClass.primary
      case eshSuorituksenTyyppi.secondaryLower:
        return eshSuorituksenClass.secondaryLowerVuosiluokka
      case eshSuorituksenTyyppi.secondaryUpper:
        return eshSuorituksenClass.secondaryUpperVuosiluokka
      default:
        throw new Error(
          `suoritusProtypeKey not found for ${opiskeluoikeudenTyyppi}, ${suorituksenTyyppi}`
        )
    }
  } else if (opiskeluoikeudenTyyppi === 'ebtutkinto') {
    switch (suorituksenTyyppi) {
      case ebSuorituksenTyyppi.ebtutkinto:
        return ebSuorituksenClass.ebtutkinto
      default:
        throw new Error(
          `suoritusProtypeKey not found for ${opiskeluoikeudenTyyppi}, ${suorituksenTyyppi}`
        )
    }
  } else {
    throw new Error(
      `suoritusProtypeKey not found for ${opiskeluoikeudenTyyppi}, ${suorituksenTyyppi}`
    )
  }
}
