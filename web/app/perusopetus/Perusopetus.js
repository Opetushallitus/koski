import {findModelProperty, modelData, modelItems, modelLookup, oneOfPrototypes} from '../editor/EditorModel'
import Bacon from 'baconjs'
import Http from '../util/http'
import {arvioituTaiVahvistettu, suorituksenTyyppi} from '../suoritus/Suoritus'

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

export const isVuosiluokkaTaiPerusopetuksenOppimäärä = (suoritus) => ['perusopetuksenoppimaara', 'perusopetuksenvuosiluokka'].includes(suorituksenTyyppi(suoritus))
export const isYksilöllistetty = suoritus => modelData(suoritus, 'yksilöllistettyOppimäärä')
export const isPainotettu = suoritus => modelData(suoritus, 'painotettuOpetus')
export const isKorotus = suoritus => modelData(suoritus, 'korotus')

export const luokkaAsteenOsasuoritukset = (luokkaAste_, toimintaAlueittain) => Http.cachedGet(`/koski/api/editor/koodit/perusopetuksenluokkaaste/${luokkaAste_}/suoritukset/prefill?toimintaAlueittain=${toimintaAlueittain}`)

export const oppimääränOsasuoritukset = (suoritustyyppi, toimintaAlueittain = false) =>
  suoritustyyppi ? Http.cachedGet(`/koski/api/editor/koodit/koulutus/201101/suoritukset/prefill?tyyppi=${suoritustyyppi.koodiarvo}&toimintaAlueittain=${toimintaAlueittain}`) : Bacon.constant([])

const YksilöllistettyFootnote = {title: 'Yksilöllistetty oppimäärä', hint: '*'}
const PainotettuFootnote = {title: 'Painotettu opetus', hint: '**'}
const KorotusFootnote = {title: 'Perusopetuksen päättötodistuksen arvosanan korotus', hint: '†'}

export const footnoteDescriptions = oppiaineSuoritukset => [
  oppiaineSuoritukset.find(isYksilöllistetty) && YksilöllistettyFootnote,
  oppiaineSuoritukset.find(isPainotettu) && PainotettuFootnote,
  oppiaineSuoritukset.find(isKorotus) && KorotusFootnote
].filter(v => !!v)

export const footnotesForSuoritus = suoritus => [
  isYksilöllistetty(suoritus) && YksilöllistettyFootnote,
  isPainotettu(suoritus) && PainotettuFootnote,
  isKorotus(suoritus) && KorotusFootnote
].filter(v => !!v)

export const valmiitaSuorituksia = oppiaineSuoritukset =>
  oppiaineSuoritukset.some(oppiaine => arvioituTaiVahvistettu(oppiaine) || modelItems(oppiaine, 'osasuoritukset').some(arvioituTaiVahvistettu))

export const pakollisetTitle = 'Pakolliset oppiaineet'
export const valinnaisetTitle = 'Valinnaiset oppiaineet'
export const groupTitleForSuoritus = suoritus => modelData(suoritus).koulutusmoduuli.pakollinen ? pakollisetTitle : valinnaisetTitle

