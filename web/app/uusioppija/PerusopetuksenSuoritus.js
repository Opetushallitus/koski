import {koodiarvoMatch} from './koodisto'
import Http from '../http'
import {modelData} from '../editor/EditorModel'

export const oppiaineetP = suoritustyyppiAtom => suoritustyyppiAtom.flatMapLatest((tyyppi) => {
  if (koodiarvoMatch('perusopetuksenoppimaara', 'aikuistenperusopetuksenoppimaara')(tyyppi)) {
    return Http.cachedGet(`/koski/api/editor/suoritukset/prefill/koulutus/201101?tyyppi=${tyyppi.koodiarvo}`).map(modelData)
  } else {
    return []
  }
}).toProperty()

export const makeSuoritus = (oppilaitos, oppimäärä, peruste, oppiaineet, suorituskieli, oppiaineenSuoritus) => {
  if (oppilaitos && peruste && koodiarvoMatch('perusopetuksenoppimaara', 'aikuistenperusopetuksenoppimaara')(oppimäärä) && suorituskieli) {
    return makePerusopetuksenOppimääränSuoritus(oppilaitos, oppimäärä, peruste, oppiaineet, suorituskieli)
  } else if (koodiarvoMatch('aikuistenperusopetuksenoppimaaranalkuvaihe')(oppimäärä)) {
    return makeAikuistenPerusopetuksenAlkuvaiheenSuoritus(oppilaitos, oppimäärä, peruste, oppiaineet, suorituskieli)
  } else if (koodiarvoMatch('perusopetuksenoppiaineenoppimaara')(oppimäärä) && oppiaineenSuoritus) {
    return oppiaineenSuoritus
  }
}

const makePerusopetuksenOppimääränSuoritus = (oppilaitos, oppimäärä, peruste, oppiaineet, suorituskieli) => {
  return {
    suorituskieli : suorituskieli,
    koulutusmoduuli: {
      tunniste: {
        koodiarvo: '201101',
        koodistoUri: 'koulutus'
      },
      perusteenDiaarinumero: peruste
    },
    toimipiste: oppilaitos,
    tila: { koodistoUri: 'suorituksentila', koodiarvo: 'KESKEN'},
    suoritustapa: { koodistoUri: 'perusopetuksensuoritustapa', koodiarvo: 'koulutus'},
    tyyppi: oppimäärä,
    osasuoritukset: oppiaineet
  }
}

const makeAikuistenPerusopetuksenAlkuvaiheenSuoritus = (oppilaitos, oppimäärä, peruste, oppiaineet, suorituskieli) => {
  return {
    suorituskieli : suorituskieli,
    koulutusmoduuli: {
      tunniste: {
        koodiarvo: 'aikuistenperusopetuksenoppimaaranalkuvaihe',
        koodistoUri: 'suorituksentyyppi'
      },
      perusteenDiaarinumero: peruste
    },
    toimipiste: oppilaitos,
    tila: { koodistoUri: 'suorituksentila', koodiarvo: 'KESKEN'},
    suoritustapa: { koodistoUri: 'perusopetuksensuoritustapa', koodiarvo: 'koulutus'},
    tyyppi: oppimäärä,
    osasuoritukset: oppiaineet
  }
}

