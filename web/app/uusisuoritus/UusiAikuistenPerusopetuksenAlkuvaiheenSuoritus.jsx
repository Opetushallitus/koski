import React from 'baret'
import Bacon from 'baconjs'
import {
  aikuistenPerusopetuksenAlkuvaiheenSuoritus,
  aikuistenPerusopetuksenOppimääränSuoritus,
  copyToimipiste,
  newSuoritusProto
} from '../suoritus/Suoritus'
import { modelData } from '../editor/EditorModel'
import Text from '../i18n/Text'

export default {
  createSuoritus: (opiskeluoikeus) => {
    const proto = newSuoritusProto(
      opiskeluoikeus,
      'aikuistenperusopetuksenalkuvaiheensuoritus'
    )
    return Bacon.once(
      copyToimipiste(
        aikuistenPerusopetuksenOppimääränSuoritus(opiskeluoikeus),
        proto
      )
    )
  },
  canAddSuoritus: (opiskeluoikeus) => {
    return (
      modelData(opiskeluoikeus, 'tyyppi.koodiarvo') == 'aikuistenperusopetus' &&
      !!aikuistenPerusopetuksenOppimääränSuoritus(opiskeluoikeus) &&
      !aikuistenPerusopetuksenAlkuvaiheenSuoritus(opiskeluoikeus)
    )
  },
  addSuoritusTitle: () => <Text name="lisää opintojen alkuvaiheen suoritus" />,
  addSuoritusTitleKey: 'lisää opintojen alkuvaiheen suoritus'
}
