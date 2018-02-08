import {koodiarvoMatch} from './koodisto'
import {perusteenDiaarinumeroToOppimäärä} from '../lukio/lukio'

export const makeSuoritus = (oppilaitos, suoritustyyppi, peruste, suorituskieli) => {
  if (oppilaitos && koodiarvoMatch('lukionoppimaara')(suoritustyyppi) && peruste && suorituskieli) {
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
  }
}
