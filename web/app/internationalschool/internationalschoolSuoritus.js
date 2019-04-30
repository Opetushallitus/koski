export const makeSuoritus = (oppilaitos, grade, suorituskieli) => {
  if (!oppilaitos || !grade || !suorituskieli) return null

  return {
    suorituskieli : suorituskieli,
    koulutusmoduuli: {
      tunniste: grade
    },
    toimipiste: oppilaitos,
    tyyppi: {
      koodiarvo: suoritusTyyppi(grade),
      koodistoUri: 'suorituksentyyppi'
    }
  }
}

export const suoritusTyyppi = grade => {
  if (grade.koodiarvo === 'explorer' || grade.koodiarvo < 6) {
    return 'internationalschoolpypvuosiluokka'
  } else if (grade.koodiarvo < 11) {
    return 'internationalschoolmypvuosiluokka'
  } else {
    return 'internationalschooldiplomavuosiluokka'
  }
}

export const suoritusPrototypeKey = type => {
  switch (type) {
    case 'internationalschoolpypvuosiluokka': return 'pypvuosiluokansuoritus'
    case 'internationalschoolmypvuosiluokka': return 'mypvuosiluokansuoritus'
    case 'internationalschooldiplomavuosiluokka': return 'diplomavuosiluokansuoritus'
  }
}
