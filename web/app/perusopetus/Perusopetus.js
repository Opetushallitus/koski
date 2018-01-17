import R from 'ramda'
import {modelData, modelItems} from '../editor/EditorModel'
import Bacon from 'baconjs'
import Http from '../util/http'
import {arvioituTaiVahvistettu, suorituksenTyyppi} from '../suoritus/Suoritus'

export const isToimintaAlueittain = (suoritus) => !!modelData(suoritus.context.opiskeluoikeus, 'lisätiedot.erityisenTuenPäätös.opiskeleeToimintaAlueittain')
export const isYsiluokka = (suoritus) => luokkaAste(suoritus) == '9'

export const isPäättötodistus = (suoritus) => {
  let tunniste = modelData(suoritus, 'koulutusmoduuli.tunniste')
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

export const luokkaAsteenOsasuoritukset = (luokkaAste_, toimintaAlueittain) => Http.cachedGet(`/koski/api/editor/suoritukset/prefill/perusopetuksenluokkaaste/${luokkaAste_}?toimintaAlueittain=${toimintaAlueittain}`)

export const oppimääränOsasuoritukset = (suoritustyyppi, toimintaAlueittain = false) =>
  suoritustyyppi ? Http.cachedGet(`/koski/api/editor/suoritukset/prefill/koulutus/201101?tyyppi=${suoritustyyppi.koodiarvo}&toimintaAlueittain=${toimintaAlueittain}`) : Bacon.constant([])

export const hasEsitäyttöOppiaineSuoritukset = (model, suoritukset) => suorituksetEquals(esitäyttöOsasuoritukset(model, false), suoritukset)
export const hasEsitäyttöToimintaAlueSuoritukset = (model, suoritukset) => suorituksetEquals(esitäyttöOsasuoritukset(model, true), suoritukset)

const suorituksetEquals = (esitäyttöOsasuorituksetP, oppiaineSuoritukset) =>
  esitäyttöOsasuorituksetP.map(esitäyttö => {
    // esimerkkidatan tyyppi ei sisällä nimi ja versiotietoja, poistetaan tyyppi koska se ei ole relevanttia vertailussa
    let esitäyttöSuoritukset = esitäyttö.value.map(o => R.dissoc('tyyppi', modelData(o)))
    let suoritukset = oppiaineSuoritukset.map(s => R.dissoc('tyyppi', modelData(s)))
    return R.equals(esitäyttöSuoritukset, suoritukset)
  })

export const esitäyttöOsasuoritukset = (model, toimintaAlueittain) => isPäättötodistus(model)
  ? oppimääränOsasuoritukset(modelData(model, 'tyyppi'), toimintaAlueittain)
  : luokkaAsteenOsasuoritukset(luokkaAste(model), toimintaAlueittain)

export const valmiitaSuorituksia = oppiaineSuoritukset => {
  let valmiitaKursseja = () => oppiaineSuoritukset.flatMap(oppiaine => modelItems(oppiaine, 'osasuoritukset')).filter(arvioituTaiVahvistettu)
  return oppiaineSuoritukset.filter(arvioituTaiVahvistettu).length > 0 || valmiitaKursseja().length > 0
}
