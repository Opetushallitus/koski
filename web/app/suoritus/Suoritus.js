import {
  contextualizeSubModel, lensedModel, modelData, modelItems, modelLookup, modelSet, modelSetValues,
  modelTitle
} from '../editor/EditorModel'
import * as L from 'partial.lenses'
import * as R from 'ramda'
import {t} from '../i18n/i18n'
import {parseISODate} from '../date/date'
import {flatMapArray} from '../util/util'
import {tutkinnonNimi} from './Koulutusmoduuli'
import {isOsittaisenAmmatillisenTutkinnonYhteisenTutkinnonOsanSuoritus} from '../ammatillinen/TutkinnonOsa'

const isInPast = dateStr => parseISODate(dateStr) < new Date()

export const arvioituTaiVahvistettu = suoritus => {
  if (suoritus.value.classes.includes('paatasonsuoritus')) {
    return !!modelData(suoritus, 'vahvistus')
  } else {
    return !!modelData(suoritus, 'arviointi.0')
  }
}

export const suoritusValmis = (suoritus) => {
  if (suoritus.value.classes.includes('paatasonsuoritus')) {
    let vahvistuspäivä = modelData(suoritus, 'vahvistus.päivä')
    return vahvistuspäivä && isInPast(vahvistuspäivä)
  } else if (R.intersection(suoritus.value.classes, ['arvioinniton', 'mahdollisestiarvioinniton']).length !== 0) {
    return true
  } else {
    let arviointi = modelData(suoritus, 'arviointi.0')
    let arviointiPäivä = modelData(arviointi, 'päivä')
    return arviointi && arviointiPäivä ? isInPast(arviointiPäivä) : !!arviointi
  }
}
export const suoritusKesken = R.complement(suoritusValmis)
export const tilaText = (suoritus) => t(suoritusValmis(suoritus) ? 'Suoritus valmis' : 'Suoritus kesken')
export const tilaKoodi = (suoritus) => suoritusValmis(suoritus) ? 'valmis' : 'kesken'
export const hasArviointi = (suoritus) => !!modelData(suoritus, 'arviointi.-1')
export const hasArvosana = (suoritus) => !!modelData(suoritus, 'arviointi.-1.arvosana')
export const arviointiPuuttuu = (m) => !m.value.classes.includes('arvioinniton') && !hasArvosana(m)
export const onKeskeneräisiäOsasuorituksia  = (suoritus) => {
  return keskeneräisetOsasuoritukset(suoritus).length > 0
}
export const keskeneräisetOsasuoritukset = (suoritus) => osasuoritukset(suoritus).filter(s => {
  if (isOsittaisenAmmatillisenTutkinnonYhteisenTutkinnonOsanSuoritus(s) && suoritusKesken(s)) {
    return !osasuorituksetVahvistettu(s)
  } else {
    return R.either(suoritusKesken, onKeskeneräisiäOsasuorituksia)(s)
  }
})
export const osasuorituksetVahvistettu = s => R.and(R.all(suoritusValmis)(osasuoritukset(s)), osasuoritukset(s).length > 0)
export const osasuoritukset = (suoritus) => modelItems(suoritus, 'osasuoritukset')
export const rekursiivisetOsasuoritukset = (suoritus) => flatMapArray(osasuoritukset(suoritus), s => [s].concat(rekursiivisetOsasuoritukset(s)))
export const suorituksenTyyppi = suoritus => suoritus && modelData(suoritus, 'tyyppi').koodiarvo

export const suoritusTitle = (suoritus) => {
  let title = modelTitle(tutkinnonNimi(modelLookup(suoritus, 'koulutusmoduuli')))
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

export const copySuorituskieli = (from, to) => modelSet(to, modelLookup(from, 'suorituskieli'), 'suorituskieli')

export const copyToimipiste = (from, to) => modelSet(to, modelLookup(from, 'toimipiste'), 'toimipiste')

export const opiskeluoikeudenSuoritusByTyyppi = (tyyppi) => (opiskeluoikeus) => modelItems(opiskeluoikeus, 'suoritukset').find(suoritus => suorituksenTyyppi(suoritus) == tyyppi)
export const aikuistenPerusopetuksenOppimääränSuoritus = opiskeluoikeudenSuoritusByTyyppi('aikuistenperusopetuksenoppimaara')
export const aikuistenPerusopetuksenAlkuvaiheenSuoritus = opiskeluoikeudenSuoritusByTyyppi('aikuistenperusopetuksenoppimaaranalkuvaihe')
export const perusopetuksenOppiaineenOppimääränSuoritus = opiskeluoikeudenSuoritusByTyyppi('perusopetuksenoppiaineenoppimaara')
export const nuortenPerusopetuksenOppiaineenOppimääränSuoritus = opiskeluoikeudenSuoritusByTyyppi('nuortenperusopetuksenoppiaineenoppimaara')
export const näyttötutkintoonValmistavanKoulutuksenSuoritus = opiskeluoikeudenSuoritusByTyyppi('nayttotutkintoonvalmistavakoulutus')
export const ammatillisenTutkinnonSuoritus = opiskeluoikeudenSuoritusByTyyppi('ammatillinentutkinto')
export const preIBSuoritus = opiskeluoikeudenSuoritusByTyyppi('preiboppimaara')
export const ibTutkinnonSuoritus = opiskeluoikeudenSuoritusByTyyppi('ibtutkinto')
export const valmistavanDIAVaiheenSuoritus = opiskeluoikeudenSuoritusByTyyppi('diavalmistavavaihe')
export const diaTutkinnonSuoritus = opiskeluoikeudenSuoritusByTyyppi('diatutkintovaihe')

export const koulutustyyppiKoodit = suoritustyyppiKoodi => {
  if (suoritustyyppiKoodi == 'perusopetuksenoppimaara' || suoritustyyppiKoodi == 'perusopetuksenvuosiluokka' || suoritustyyppiKoodi == 'nuortenperusopetuksenoppiaineenoppimaara') {
    return ['16']
  }
  if (suoritustyyppiKoodi == 'aikuistenperusopetuksenoppimaara' || suoritustyyppiKoodi == 'perusopetuksenoppiaineenoppimaara' || suoritustyyppiKoodi == 'aikuistenperusopetuksenoppimaaranalkuvaihe'){
    return ['17']
  }
  if (suoritustyyppiKoodi == 'perusopetuksenlisaopetus') {
    return ['6']
  }
  if (suoritustyyppiKoodi == 'perusopetukseenvalmistavaopetus') {
    return ['22']
  }
  if (suoritustyyppiKoodi == 'esiopetuksensuoritus') {
    return ['15']
  }
  if (suoritustyyppiKoodi == 'valma') {
    return ['18']
  }
  if (suoritustyyppiKoodi == 'telma') {
    return ['5']
  }
  if (suoritustyyppiKoodi == 'lukionoppimaara' || suoritustyyppiKoodi == 'lukionoppiaineenoppimaara') {
    return ['2', '14']
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
