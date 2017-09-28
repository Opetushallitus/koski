import React from 'baret'
import {
  aikuistenPerusopetuksenAlkuvaiheenSuoritus,
  aikuistenPerusopetuksenOppimääränSuoritus,
  copyToimipiste,
  newSuoritusProto
} from '../editor/Suoritus'
import {modelData, modelSetValue} from '../editor/EditorModel'
import Text from '../Text.jsx'
import {oppimääränOsasuoritukset} from '../editor/Perusopetus'

export default {
  createSuoritus : (opiskeluoikeus) => {
    let proto = newSuoritusProto(opiskeluoikeus, 'aikuistenperusopetuksenoppimaaransuoritus')
    let toimipisteellä = copyToimipiste(aikuistenPerusopetuksenAlkuvaiheenSuoritus(opiskeluoikeus), proto)
    return oppimääränOsasuoritukset(modelData(proto, 'tyyppi').koodiarvo).map(oppiaineet => modelSetValue(toimipisteellä, oppiaineet.value, 'osasuoritukset'))
  },
  canAddSuoritus: (opiskeluoikeus) => {
    return modelData(opiskeluoikeus, 'tyyppi.koodiarvo') == 'aikuistenperusopetus'
      && !!aikuistenPerusopetuksenAlkuvaiheenSuoritus(opiskeluoikeus)
      && !aikuistenPerusopetuksenOppimääränSuoritus(opiskeluoikeus)
  },
  addSuoritusTitle: () => <Text name="lisää opintojen päättövaiheen suoritus"/>
}