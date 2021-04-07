import React from 'baret'
import {copyToimipiste, newSuoritusProto, preIBSuoritus} from '../suoritus/Suoritus'
import {modelData, modelSetValue} from '../editor/EditorModel'
import Text from '../i18n/Text'
import {oppimääränOsasuoritukset} from '../perusopetus/Perusopetus'

export default {
  createSuoritus : (opiskeluoikeus) => {
    let proto = newSuoritusProto(opiskeluoikeus, 'ibtutkinnonsuoritus')
    let toimipisteellä = copyToimipiste(preIBSuoritus(opiskeluoikeus), proto)
    return oppimääränOsasuoritukset(modelData(proto, 'tyyppi')).map(oppiaineet => modelSetValue(toimipisteellä, oppiaineet.value, 'osasuoritukset'))
  },
  canAddSuoritus: () => true,
  addSuoritusTitle: () => <Text name="lisää opintojen päättövaiheen suoritus"/>
}

