import {modelData} from './EditorModel'
import Http from '../http'

export const isToimintaAlueittain = (suoritus) => !!modelData(suoritus.context.opiskeluoikeus, 'lisätiedot.erityisenTuenPäätös.opiskeleeToimintaAlueittain')
export const isYsiluokka = (suoritus) => luokkaAste(suoritus) == '9'
export const isPerusopetuksenOppimäärä = (suoritus) => {
  let tunniste = modelData(suoritus, 'koulutusmoduuli.tunniste')
  return tunniste.koodistoUri == 'koulutus' && tunniste.koodiarvo == '201101'
}
export const jääLuokalle = (suoritus) => modelData(suoritus, 'jääLuokalle')

export const luokkaAste = (suoritus) => {
  let tunniste = modelData(suoritus, 'koulutusmoduuli.tunniste')
  return tunniste.koodistoUri == 'perusopetuksenluokkaaste' ? tunniste.koodiarvo : undefined
}

export const luokkaAsteenOsasuoritukset = (luokkaAste_, toimintaAlueittain) => Http.cachedGet(`/koski/api/editor/suoritukset/prefill/perusopetuksenluokkaaste/${luokkaAste_}?toimintaAlueittain=${toimintaAlueittain}`)
