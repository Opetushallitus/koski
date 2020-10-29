import {perusteenDiaarinumeroToOppimäärä} from '../lukio/lukio'

export const makeSuoritus = (oppilaitos, suoritustyyppi, peruste, suorituskieli, oppiaineenSuoritus) => {
  if (!oppilaitos || !suoritustyyppi || !peruste || ! suorituskieli) return null

  switch (suoritustyyppi.koodiarvo) {
    case 'lukionoppimaara':
      return {
        suorituskieli : suorituskieli,
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '309902',
            koodistoUri: 'koulutus'
          },
          perusteenDiaarinumero: peruste
        },
        oppimäärä: {
          koodiarvo: perusteenDiaarinumeroToOppimäärä(peruste),
          koodistoUri: 'lukionoppimaara'
        },
        toimipiste: oppilaitos,
        tyyppi: suoritustyyppi
      }

    case 'lukionoppiaineidenoppimaarat2019':
      return {
        suorituskieli : suorituskieli,
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: 'lukionoppiaineidenoppimaarat2019'
          },
          perusteenDiaarinumero: peruste
        },
        oppimäärä: {
          koodiarvo: perusteenDiaarinumeroToOppimäärä(peruste),
          koodistoUri: 'lukionoppimaara'
        },
        toimipiste: oppilaitos,
        tyyppi: suoritustyyppi
      }

    case 'lukionoppiaineenoppimaara':
      return oppiaineenSuoritus
  }
}
