import {
  contextualizeSubModel, lensedModel, modelData, modelItems, modelLookup, modelSet, modelSetValues,
  modelTitle
} from './EditorModel'
import * as L from 'partial.lenses'
import R from 'ramda'
import {t} from '../i18n'
import {parseISODate} from '../date'

const isInPast = dateStr => parseISODate(dateStr) < new Date()
export const suoritusValmis = (suoritus) => {
  if (suoritus.value.classes.includes('paatasonsuoritus')) {
    let vahvistuspäivä = modelData(suoritus, 'vahvistus.päivä')
    return vahvistuspäivä && isInPast(vahvistuspäivä)
  } else {
    let arviointi = modelData(suoritus, 'arviointi.0')
    let arviointiPäivä = modelData(arviointi, 'päivä')
    return arviointi && arviointiPäivä ? isInPast(arviointiPäivä) : !!arviointi
  }
}
export const suorituksellaVahvistus = suoritus => !!modelData(suoritus, 'vahvistus')
export const suoritusKesken = R.complement(suoritusValmis)
export const tilaText = (suoritus) => t(suoritusValmis(suoritus) ? 'Suoritus valmis' : 'Suoritus kesken')
export const tilaKoodi = (suoritus) => suoritusValmis(suoritus) ? 'valmis' : 'kesken'
export const hasArvosana = (suoritus) => !!modelData(suoritus, 'arviointi.-1.arvosana')
export const arviointiPuuttuu = (m) => !m.value.classes.includes('arvioinniton') && !hasArvosana(m)
export const onKeskeneräisiäOsasuorituksia  = (suoritus) => {
  return keskeneräisetOsasuoritukset(suoritus).length > 0
}
export const keskeneräisetOsasuoritukset = (suoritus) => osasuoritukset(suoritus).filter(R.either(suoritusKesken, onKeskeneräisiäOsasuorituksia))
export const osasuoritukset = (suoritus) => modelItems(suoritus, 'osasuoritukset')
export const rekursiivisetOsasuoritukset = (suoritus) => osasuoritukset(suoritus).flatMap(s => [s].concat(rekursiivisetOsasuoritukset(s)))
export const suorituksenTyyppi = (suoritus) => modelData(suoritus, 'tyyppi').koodiarvo

export const suoritusTitle = (suoritus) => {
  let title = modelTitle(suoritus, 'koulutusmoduuli.tunniste')
  switch(suorituksenTyyppi(suoritus)) {
    case 'ammatillinentutkintoosittainen': return title + t(', osittainen')
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

export const koulutustyyppiKoodi = suoritustyyppiKoodi => {
  if (suoritustyyppiKoodi == 'perusopetuksenoppimaara' || suoritustyyppiKoodi == 'perusopetuksenvuosiluokka') {
    return '16'
  }
  if (suoritustyyppiKoodi == 'aikuistenperusopetuksenoppimaara' || suoritustyyppiKoodi == 'perusopetuksenoppiaineenoppimaara' || suoritustyyppiKoodi == 'aikuistenperusopetuksenoppimaaranalkuvaihe'){
    return '17'
  }
  if (suoritustyyppiKoodi == 'perusopetuksenlisaopetus') {
    return '6'
  }
  if (suoritustyyppiKoodi == 'perusopetukseenvalmistavaopetus') {
    return '22'
  }
  if (suoritustyyppiKoodi == 'esiopetuksensuoritus') {
    return '15'
  }
}

export const fixArviointi = (model) => {
  return lensedModel(model, L.rewrite(m => {
    if (!hasArvosana(m)) {
      // Arvosana puuttuu -> poistetaan arviointi, vahvistus ja asetetaan tilaksi KESKEN
      return modelSetValues(m, { arviointi: undefined, vahvistus: undefined })
    }
    return m
  }))
}
