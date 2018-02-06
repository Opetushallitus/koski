import {modelData} from '../editor/EditorModel'
import Bacon from 'baconjs'
import Http from '../util/http'
import {suorituksenTyyppi} from '../suoritus/Suoritus'

export const isToimintaAlueittain = (suoritus) => !!modelData(suoritus && suoritus.context.opiskeluoikeus, 'lisätiedot.erityisenTuenPäätös.opiskeleeToimintaAlueittain')
export const isYsiluokka = (suoritus) => luokkaAste(suoritus) == '9'

export const isPäättötodistus = (suoritus) => {
  const tunniste = modelData(suoritus, 'koulutusmoduuli.tunniste')
  return tunniste.koodistoUri === 'koulutus' && tunniste.koodiarvo === '201101'
}

export const isPerusopetuksenOppimäärä = (suoritus) => {
  return ['perusopetuksenoppimaara', 'aikuistenperusopetuksenoppimaara'].includes(suorituksenTyyppi(suoritus))
}
export const jääLuokalle = (suoritus) => modelData(suoritus, 'jääLuokalle')
export const luokkaAste = (suoritus) => {
  let tunniste = modelData(suoritus, 'koulutusmoduuli.tunniste')
  return tunniste.koodistoUri == 'perusopetuksenluokkaaste' ? tunniste.koodiarvo : undefined
}
export const isYksilöllistetty = suoritus => modelData(suoritus, 'yksilöllistettyOppimäärä')
export const isPainotettu = suoritus => modelData(suoritus, 'painotettuOpetus')
export const isKorotus = suoritus => modelData(suoritus, 'korotus')

export const luokkaAsteenOsasuoritukset = (luokkaAste_, toimintaAlueittain) => Http.cachedGet(`/koski/api/editor/suoritukset/prefill/perusopetuksenluokkaaste/${luokkaAste_}?toimintaAlueittain=${toimintaAlueittain}`)

export const oppimääränOsasuoritukset = (suoritustyyppi, toimintaAlueittain = false) =>
  suoritustyyppi ? Http.cachedGet(`/koski/api/editor/suoritukset/prefill/koulutus/201101?tyyppi=${suoritustyyppi.koodiarvo}&toimintaAlueittain=${toimintaAlueittain}`) : Bacon.constant([])
