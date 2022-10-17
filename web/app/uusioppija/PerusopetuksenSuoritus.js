import { koodiarvoMatch } from './koodisto'
import { modelData } from '../editor/EditorModel'
import { oppimääränOsasuoritukset } from '../perusopetus/Perusopetus'

export const oppiaineetP = (suoritustyyppiAtom) =>
  suoritustyyppiAtom
    .flatMapLatest((tyyppi) => oppimääränOsasuoritukset(tyyppi).map(modelData))
    .toProperty()

export const makeSuoritus = (
  oppilaitos,
  oppimäärä,
  peruste,
  oppiaineet,
  suorituskieli,
  oppiaineenSuoritus
) => {
  if (
    oppilaitos &&
    peruste &&
    koodiarvoMatch(
      'perusopetuksenoppimaara',
      'aikuistenperusopetuksenoppimaara'
    )(oppimäärä) &&
    suorituskieli
  ) {
    return makePerusopetuksenOppimääränSuoritus(
      oppilaitos,
      oppimäärä,
      peruste,
      oppiaineet,
      suorituskieli
    )
  } else if (
    koodiarvoMatch('aikuistenperusopetuksenoppimaaranalkuvaihe')(oppimäärä)
  ) {
    return makeAikuistenPerusopetuksenAlkuvaiheenSuoritus(
      oppilaitos,
      oppimäärä,
      peruste,
      oppiaineet,
      suorituskieli
    )
  } else if (
    koodiarvoMatch(
      'perusopetuksenoppiaineenoppimaara',
      'nuortenperusopetuksenoppiaineenoppimaara'
    )(oppimäärä) &&
    oppiaineenSuoritus
  ) {
    return oppiaineenSuoritus
  }
}

const makePerusopetuksenOppimääränSuoritus = (
  oppilaitos,
  oppimäärä,
  peruste,
  oppiaineet,
  suorituskieli
) => {
  return {
    suorituskieli,
    koulutusmoduuli: {
      tunniste: {
        koodiarvo: '201101',
        koodistoUri: 'koulutus'
      },
      perusteenDiaarinumero: peruste
    },
    toimipiste: oppilaitos,
    suoritustapa: {
      koodistoUri: 'perusopetuksensuoritustapa',
      koodiarvo: 'koulutus'
    },
    tyyppi: oppimäärä,
    osasuoritukset: oppiaineet
  }
}

const makeAikuistenPerusopetuksenAlkuvaiheenSuoritus = (
  oppilaitos,
  oppimäärä,
  peruste,
  oppiaineet,
  suorituskieli
) => {
  return {
    suorituskieli,
    koulutusmoduuli: {
      tunniste: {
        koodiarvo: 'aikuistenperusopetuksenoppimaaranalkuvaihe',
        koodistoUri: 'suorituksentyyppi'
      },
      perusteenDiaarinumero: peruste
    },
    toimipiste: oppilaitos,
    suoritustapa: {
      koodistoUri: 'perusopetuksensuoritustapa',
      koodiarvo: 'koulutus'
    },
    tyyppi: oppimäärä,
    osasuoritukset: oppiaineet
  }
}
