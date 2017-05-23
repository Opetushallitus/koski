import {modelData, modelLens, modelSetValue, modelItems} from './EditorModel'
import * as L from 'partial.lenses'

export const suoritusValmis = (suoritus) => suorituksenTila(suoritus) === 'VALMIS'
export const suoritusKesken = (suoritus) => suorituksenTila(suoritus) === 'KESKEN'
export const suorituksenTila = (suoritus) => modelData(suoritus, 'tila').koodiarvo
export const hasArvosana = (suoritus) => !!modelData(suoritus, 'arviointi.-1.arvosana')
export const arvosanaLens = modelLens('arviointi.-1.arvosana')
export const arviointiPuuttuu = (m) => !m.value.classes.includes('arvioinniton') && !hasArvosana(m)
export const lastArviointiLens = modelLens('arviointi.-1')
export const tilaLens = modelLens('tila')
export const setTila = (suoritus, koodiarvo) => {
  let t = modelSetValue(L.get(tilaLens, suoritus), createTila(koodiarvo))
  return L.set(tilaLens, t, suoritus)
}
export const onKeskeneräisiäOsasuorituksia  = (suoritus) => {
  return modelItems(suoritus, 'osasuoritukset').find(suoritusKesken) != undefined
}

export const suorituksenTyyppi = (suoritus) => modelData(suoritus, 'tyyppi').koodiarvo

const createTila = (koodiarvo) => {
  if (!tilat[koodiarvo]) throw new Error('tila puuttuu: ' + koodiarvo)
  return tilat[koodiarvo]
}

const tilat = {
  VALMIS: { data: { koodiarvo: 'VALMIS', koodistoUri: 'suorituksentila' }, title: 'Suoritus valmis' },
  KESKEN: { data: { koodiarvo: 'KESKEN', koodistoUri: 'suorituksentila' }, title: 'Suoritus kesken' },
  KESKEYTYNYT: { data: { koodiarvo: 'KESKEYTYNYT', koodistoUri: 'suorituksentila' }, title: 'Suoritus keskeytynyt' }
}