export const makeSuoritus = (oppilaitos, suoritustyyppi, peruste, suorituskieli) => {
  if (!oppilaitos || !suoritustyyppi || ! suorituskieli) return null

  switch (suoritustyyppi.koodiarvo) {
    case 'vstoppivelvollisillesuunnattukoulutus':
      return {
        suorituskieli : suorituskieli,
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '999909',
            koodistoUri: 'koulutus'
          },
          perusteenDiaarinumero: peruste
        },
        // TODO: Miten tähän saadaan oikea toimipiste? Tulee virhettä "<oppilaitos> ei ole <toimipiste>" jos käytetään suoraan oppilaitosta
        toimipiste: {"oid":"1.2.246.562.10.78513447389","nimi":{"fi":"Varsinais-Suomen kansanopisto","sv":"Varsinais-Suomen kansanopisto","en":"Varsinais-Suomen kansanopisto"}},
        tyyppi: suoritustyyppi
      }
  }
}
