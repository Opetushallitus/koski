export const makeSuoritus = (oppilaitos, suoritustyyppi, suorituskieli) => {
  if (!oppilaitos || !suoritustyyppi || !suorituskieli) return null

  switch (suoritustyyppi.koodiarvo) {
    case 'ibtutkinto':
      return {
        suorituskieli,
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
        suorituskieli,
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: 'preiboppimaara',
            koodistoUri: 'suorituksentyyppi'
          }
        },
        toimipiste: oppilaitos,
        tyyppi: suoritustyyppi
      }
  }
}
