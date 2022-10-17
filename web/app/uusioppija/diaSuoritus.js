export const makeSuoritus = (oppilaitos, suoritustyyppi, suorituskieli) => {
  if (!oppilaitos || !suoritustyyppi || !suorituskieli) return null

  switch (suoritustyyppi.koodiarvo) {
    case 'diatutkintovaihe':
      return {
        suorituskieli,
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '301103',
            koodistoUri: 'koulutus'
          }
        },
        toimipiste: oppilaitos,
        tyyppi: suoritustyyppi
      }

    case 'diavalmistavavaihe':
      return {
        suorituskieli,
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: 'diavalmistavavaihe',
            koodistoUri: 'suorituksentyyppi'
          }
        },
        toimipiste: oppilaitos,
        tyyppi: suoritustyyppi
      }
  }
}
