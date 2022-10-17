import { perusteenDiaarinumeroToOppimäärä } from '../lukio/lukio'

export const makeSuoritus = (
  oppilaitos,
  suoritustyyppi,
  peruste,
  suorituskieli,
  oppiaineenSuoritus
) => {
  if (!oppilaitos || !suoritustyyppi || !peruste || !suorituskieli) return null

  switch (suoritustyyppi.koodiarvo) {
    case 'lukionoppimaara':
      return {
        suorituskieli,
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

    case 'lukionaineopinnot':
      return {
        suorituskieli,
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: 'lukionaineopinnot'
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
