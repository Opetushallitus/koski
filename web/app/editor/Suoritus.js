import {modelData, modelLens, modelSetValue} from './EditorModel'
import * as L from 'partial.lenses'

export const suoritusValmis = (suoritus) => modelData(suoritus, 'tila').koodiarvo === 'VALMIS'
export const hasArvosana = (suoritus) => !!modelData(suoritus, 'arviointi.-1.arvosana')
export const arvosanaLens = modelLens('arviointi.-1.arvosana')
export const tilaLens = modelLens('tila')
export const setTila = (suoritus, koodiarvo) => {
  let t = modelSetValue(L.get(tilaLens, suoritus), createTila(koodiarvo))
  return L.set(tilaLens, t, suoritus)
}

const createTila = (koodiarvo) => {
  if (!tilat[koodiarvo]) throw new Error('tila puuttuu: ' + koodiarvo)
  return tilat[koodiarvo]
}

const tilat = {
  VALMIS: { data: { koodiarvo: 'VALMIS', koodistoUri: 'suorituksentila' }, title: 'Suoritus valmis' }
}