import {
  contextualizeSubModel,
  lensedModel,
  modelData,
  modelItems,
  modelLens,
  modelLookup, modelSet,
  modelSetValue,
  modelSetValues,
  modelTitle
} from './EditorModel'
import * as L from 'partial.lenses'
import R from 'ramda'
import {suorituksentilaKoodisto, toKoodistoEnumValue} from '../koodistot'

export const suoritusValmis = (suoritus) => suorituksenTila(suoritus) === 'VALMIS'
export const suoritusKesken = (suoritus) => suorituksenTila(suoritus) === 'KESKEN'
export const suorituksenTila = (suoritus) => modelData(suoritus, 'tila').koodiarvo
export const hasArvosana = (suoritus) => !!modelData(suoritus, 'arviointi.-1.arvosana')
export const arviointiPuuttuu = (m) => !m.value.classes.includes('arvioinniton') && !hasArvosana(m)
export const tilaLens = modelLens('tila')
export const setTila = (suoritus, koodiarvo) => {
  let tila = modelSetValue(L.get(tilaLens, suoritus), createTila(koodiarvo))
  return L.set(tilaLens, tila, suoritus)
}
export const onKeskeneräisiäOsasuorituksia  = (suoritus) => {
  return keskeneräisetOsasuoritukset(suoritus).length > 0
}

export const keskeneräisetOsasuoritukset = (suoritus) => osasuoritukset(suoritus).filter(R.either(suoritusKesken, onKeskeneräisiäOsasuorituksia))
export const osasuoritukset = (suoritus) => modelItems(suoritus, 'osasuoritukset')
export const rekursiivisetOsasuoritukset = (suoritus) => osasuoritukset(suoritus).flatMap(s => [s].concat(rekursiivisetOsasuoritukset(s)))

export const suorituksenTyyppi = (suoritus) => modelData(suoritus, 'tyyppi').koodiarvo

const createTila = (koodiarvo) => {
  if (!tilat[koodiarvo]) throw new Error('tila puuttuu: ' + koodiarvo)
  return tilat[koodiarvo]
}

const tilat = R.fromPairs(R.toPairs(suorituksentilaKoodisto).map(([key, value]) => ([key, toKoodistoEnumValue('suorituksentila', key, value)])))

export const fixTila = (model) => {
  return lensedModel(model, L.rewrite(m => {
    if (hasArvosana(m) && !suoritusValmis(m)) {
      // Arvosana annettu -> asetetaan tila VALMIS
      return setTila(m, 'VALMIS')
    }
    if (!hasArvosana(m)) {
      // Arvosana puuttuu -> poistetaan arviointi, vahvistus ja asetetaan tilaksi KESKEN
      return modelSetValues(m, { arviointi: undefined, vahvistus: undefined, tila: createTila('KESKEN')})
    }
    return m
  }))
}

export const suoritusTitle = (suoritus) => {
  let title = modelTitle(suoritus, 'koulutusmoduuli.tunniste')
  switch(suorituksenTyyppi(suoritus)) {
    case 'ammatillinentutkintoosittainen': return `${title}, osittainen` // TODO: i18n
    case 'aikuistenperusopetuksenoppimaara': return modelTitle(suoritus, 'tyyppi')
    default: return title
  }
}

export const newSuoritusProto = (opiskeluoikeus, prototypeKey) => {
  let suoritukset = modelLookup(opiskeluoikeus, 'suoritukset')
  let indexForNewItem = modelItems(suoritukset).length
  let selectedProto = contextualizeSubModel(suoritukset.arrayPrototype, suoritukset, indexForNewItem).oneOfPrototypes.find(p => p.key === prototypeKey)
  return contextualizeSubModel(selectedProto, suoritukset, indexForNewItem)
}

export const copyToimipiste = (from, to) => modelSet(to, modelLookup(from, 'toimipiste'), 'toimipiste')

export const aikuistenPerusopetuksenOppimääränSuoritus = (opiskeluoikeus) => modelItems(opiskeluoikeus, 'suoritukset').find(suoritus => suorituksenTyyppi(suoritus) == 'aikuistenperusopetuksenoppimaara')
export const aikuistenPerusopetuksenAlkuvaiheenSuoritus = (opiskeluoikeus) => modelItems(opiskeluoikeus, 'suoritukset').find(suoritus => suorituksenTyyppi(suoritus) == 'aikuistenperusopetuksenoppimaaranalkuvaihe')
export const perusopetuksenOppiaineenOppimääränSuoritus = (opiskeluoikeus) => modelItems(opiskeluoikeus, 'suoritukset').find(suoritus => suorituksenTyyppi(suoritus) == 'perusopetuksenoppiaineenoppimaara')