import React from 'baret'
import {
  aikuistenPerusopetuksenAlkuvaiheenSuoritus,
  aikuistenPerusopetuksenOppimääränSuoritus,
  copyToimipiste,
  newSuoritusProto
} from '../editor/Suoritus'
import {modelData} from '../editor/EditorModel'
import Text from '../Text.jsx'

export default {
  createSuoritus : (opiskeluoikeus) => {
    let proto = newSuoritusProto(opiskeluoikeus, 'aikuistenperusopetuksenoppimaaransuoritus')
    return copyToimipiste(aikuistenPerusopetuksenAlkuvaiheenSuoritus(opiskeluoikeus), proto)
  },
  canAddSuoritus: (opiskeluoikeus) => {
    return modelData(opiskeluoikeus, 'tyyppi.koodiarvo') == 'aikuistenperusopetus'
      && !!aikuistenPerusopetuksenAlkuvaiheenSuoritus(opiskeluoikeus)
      && !aikuistenPerusopetuksenOppimääränSuoritus(opiskeluoikeus)
  },
  addSuoritusTitle: () => <Text name="lisää opintojen päättövaiheen suoritus"/>
}