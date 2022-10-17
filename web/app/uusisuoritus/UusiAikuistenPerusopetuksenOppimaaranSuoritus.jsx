import React from 'baret'
import {
  aikuistenPerusopetuksenAlkuvaiheenSuoritus,
  aikuistenPerusopetuksenOppimääränSuoritus,
  copyToimipiste,
  newSuoritusProto
} from '../suoritus/Suoritus'
import { modelData, modelSetValue } from '../editor/EditorModel'
import Text from '../i18n/Text'
import { oppimääränOsasuoritukset } from '../perusopetus/Perusopetus'

export default {
  createSuoritus: (opiskeluoikeus) => {
    const proto = newSuoritusProto(
      opiskeluoikeus,
      'aikuistenperusopetuksenoppimaaransuoritus'
    )
    const toimipisteellä = copyToimipiste(
      aikuistenPerusopetuksenAlkuvaiheenSuoritus(opiskeluoikeus),
      proto
    )
    return oppimääränOsasuoritukset(modelData(proto, 'tyyppi')).map(
      (oppiaineet) =>
        modelSetValue(toimipisteellä, oppiaineet.value, 'osasuoritukset')
    )
  },
  canAddSuoritus: (opiskeluoikeus) => {
    return (
      modelData(opiskeluoikeus, 'tyyppi.koodiarvo') == 'aikuistenperusopetus' &&
      !!aikuistenPerusopetuksenAlkuvaiheenSuoritus(opiskeluoikeus) &&
      !aikuistenPerusopetuksenOppimääränSuoritus(opiskeluoikeus)
    )
  },
  addSuoritusTitle: () => <Text name="lisää opintojen päättövaiheen suoritus" />
}
