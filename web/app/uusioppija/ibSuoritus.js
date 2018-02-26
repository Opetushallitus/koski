export const makeSuoritus = (oppilaitos, suoritustyyppi, suorituskieli) => {
  if (!oppilaitos || !suoritustyyppi || ! suorituskieli) return null

  switch (suoritustyyppi.koodiarvo) {
    case 'ibtutkinto':
      return {
        suorituskieli : suorituskieli,
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '301102',
            koodistoUri: 'koulutus'
          }
        },
        toimipiste: oppilaitos,
        tyyppi: suoritustyyppi
      }

    case 'preiboppimaara':
      return {
        suorituskieli : suorituskieli,
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: 'preiboppimaara'
          }
        },
        toimipiste: oppilaitos,
        tyyppi: suoritustyyppi
      }
  }
}
