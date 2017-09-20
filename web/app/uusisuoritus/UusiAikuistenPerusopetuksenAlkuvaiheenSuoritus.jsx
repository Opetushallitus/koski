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
    let proto = newSuoritusProto(opiskeluoikeus, 'aikuistenperusopetuksenalkuvaiheensuoritus')
    return copyToimipiste(aikuistenPerusopetuksenOppimääränSuoritus(opiskeluoikeus), proto)
  },
  canAddSuoritus: (opiskeluoikeus) => {
    return modelData(opiskeluoikeus, 'tyyppi.koodiarvo') == 'aikuistenperusopetus'
      && !!aikuistenPerusopetuksenOppimääränSuoritus(opiskeluoikeus)
      && !aikuistenPerusopetuksenAlkuvaiheenSuoritus(opiskeluoikeus)
  },
  addSuoritusTitle: () => <Text name="lisää opintojen alkuvaiheen suoritus"/>
}