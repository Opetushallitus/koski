import React from 'baret'
import {
  copyToimipiste,
  ibTutkinnonSuoritus,
  newSuoritusProto,
  preIBSuoritus
} from '../suoritus/Suoritus'
import { modelData, modelSetValue } from '../editor/EditorModel'
import Text from '../i18n/Text'
import { oppimääränOsasuoritukset } from '../perusopetus/Perusopetus'

const suoritus = (opiskeluoikeus) =>
  oppimääränOsasuoritukset(modelData(opiskeluoikeus, 'tyyppi')).map(
    (oppiaineet) =>
      modelSetValue(opiskeluoikeus, oppiaineet.value, 'osasuoritukset')
  )
const isIBTutkinto = (opiskeluoikeus) =>
  modelData(opiskeluoikeus, 'tyyppi.koodiarvo') === 'ibtutkinto'

export const UusiIBTutkinnonSuoritus = {
  createSuoritus: (opiskeluoikeus) => {
    const proto = newSuoritusProto(opiskeluoikeus, 'ibtutkinnonsuoritus')
    const toimipisteellä = copyToimipiste(preIBSuoritus(opiskeluoikeus), proto)
    return suoritus(toimipisteellä)
  },
  canAddSuoritus: (opiskeluoikeus) =>
    isIBTutkinto(opiskeluoikeus) && !ibTutkinnonSuoritus(opiskeluoikeus),
  addSuoritusTitle: () => <Text name="lisää IB-tutkinnon suoritus" />,
  addSuoritusTitleKey: 'lisää IB-tutkinnon suoritus'
}

export const UusiPreIBSuoritus = {
  createSuoritus: (opiskeluoikeus) => {
    const proto = newSuoritusProto(opiskeluoikeus, 'preibsuoritus2015')
    const toimipisteellä = copyToimipiste(
      ibTutkinnonSuoritus(opiskeluoikeus),
      proto
    )
    return suoritus(toimipisteellä)
  },
  canAddSuoritus: (opiskeluoikeus) =>
    isIBTutkinto(opiskeluoikeus) && !preIBSuoritus(opiskeluoikeus),
  addSuoritusTitle: () => <Text name="lisää pre-IB-suoritus" />,
  addSuoritusTitleKey: 'lisää pre-IB-suoritus'
}
