import { formatISODate } from '../date/date'

export const makeSuoritus = (
  oppilaitos,
  grade,
  alkamispäivä,
  suorituskieli
) => {
  if (!oppilaitos || !grade || !grade || !suorituskieli) return null

  return {
    suorituskieli,
    koulutusmoduuli: mkKoulutusmoduuli(grade),
    toimipiste: oppilaitos,
    alkamispäivä: formatISODate(alkamispäivä),
    tyyppi: {
      koodiarvo: suoritusTyyppi(grade),
      koodistoUri: 'suorituksentyyppi'
    }
  }
}

const mkKoulutusmoduuli = (grade) => {
  return grade.koodiarvo === 'explorer' || grade.koodiarvo < 11
    ? { tunniste: grade }
    : {
        tunniste: grade,
        diplomaType: {
          koodistoUri: 'internationalschooldiplomatype',
          koodiarvo: 'ib'
        }
      }
}

export const suoritusTyyppi = (grade) => {
  if (grade.koodiarvo === 'explorer' || grade.koodiarvo < 6) {
    return 'internationalschoolpypvuosiluokka'
  } else if (grade.koodiarvo < 11) {
    return 'internationalschoolmypvuosiluokka'
  } else {
    return 'internationalschooldiplomavuosiluokka'
  }
}

export const suoritusPrototypeKey = (type) => {
  switch (type) {
    case 'internationalschoolpypvuosiluokka':
      return 'pypvuosiluokansuoritus'
    case 'internationalschoolmypvuosiluokka':
      return 'mypvuosiluokansuoritus'
    case 'internationalschooldiplomavuosiluokka':
      return 'diplomavuosiluokansuoritus'
  }
}
