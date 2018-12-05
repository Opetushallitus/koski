import React from 'baret'
import Bacon from 'baconjs'
import {
  copyToimipiste,
  newSuoritusProto,
  diaTutkinnonSuoritus,
  valmistavanDIAVaiheenSuoritus
} from '../suoritus/Suoritus'
import {modelData} from '../editor/EditorModel'
import Text from '../i18n/Text'

const isDIATutkinto = opiskeluoikeus => modelData(opiskeluoikeus, 'tyyppi.koodiarvo') === 'diatutkinto'

export const UusiDIATutkinnonSuoritus = {
  createSuoritus: (opiskeluoikeus) => {
    const proto = newSuoritusProto(opiskeluoikeus, 'diatutkinnonsuoritus')
    const toimipisteellä = copyToimipiste(valmistavanDIAVaiheenSuoritus(opiskeluoikeus), proto)
    return Bacon.once(toimipisteellä)
  },
  canAddSuoritus: (opiskeluoikeus) => isDIATutkinto(opiskeluoikeus) && !diaTutkinnonSuoritus(opiskeluoikeus),
  addSuoritusTitle: () => <Text name="lisää DIA-tutkinnon suoritus"/>
}

export const UusiValmistavanDIAVaiheenSuoritus = {
  createSuoritus: (opiskeluoikeus) => {
    const proto = newSuoritusProto(opiskeluoikeus, 'diavalmistavanvaiheensuoritus')
    const toimipisteellä = copyToimipiste(diaTutkinnonSuoritus(opiskeluoikeus), proto)
    return Bacon.once(toimipisteellä)
  },
  canAddSuoritus: (opiskeluoikeus) => isDIATutkinto(opiskeluoikeus) && !valmistavanDIAVaiheenSuoritus(opiskeluoikeus),
  addSuoritusTitle: () => <Text name="lisää valmistavan DIA-vaiheen suoritus"/>
}
