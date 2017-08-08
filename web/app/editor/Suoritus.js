import {createOptionalEmpty, lensedModel, modelData, modelItems, modelLens, modelSetValue} from './EditorModel'
import * as L from 'partial.lenses'
import R from 'ramda'
import {suorituksentilaKoodisto, toKoodistoEnumValue} from '../koodistot'

export const suoritusValmis = (suoritus) => suorituksenTila(suoritus) === 'VALMIS'
export const suoritusKesken = (suoritus) => suorituksenTila(suoritus) === 'KESKEN'
export const suorituksenTila = (suoritus) => modelData(suoritus, 'tila').koodiarvo
export const hasArvosana = (suoritus) => !!modelData(suoritus, 'arviointi.-1.arvosana')
export const arviointiPuuttuu = (m) => !m.value.classes.includes('arvioinniton') && !hasArvosana(m)
export const lastArviointiLens = modelLens('arviointi.-1')
export const tilaLens = modelLens('tila')
export const setTila = (suoritus, koodiarvo) => {
  let tila = modelSetValue(L.get(tilaLens, suoritus), createTila(koodiarvo))
  return L.set(tilaLens, tila, suoritus)
}
export const onKeskeneräisiäOsasuorituksia  = (suoritus) => {
  return modelItems(suoritus, 'osasuoritukset').find(suoritusKesken) != undefined
}

export const suorituksenTyyppi = (suoritus) => modelData(suoritus, 'tyyppi').koodiarvo

const createTila = (koodiarvo) => {
  if (!tilat[koodiarvo]) throw new Error('tila puuttuu: ' + koodiarvo)
  return tilat[koodiarvo]
}

const tilat = R.fromPairs(R.toPairs(suorituksentilaKoodisto).map(([key, value]) => ([key, toKoodistoEnumValue('suorituksentila', key, value)])))

// model wrapper, joka asettaa suorituksen tilaksi VALMIS kun sille lisätään arviointi
export const fixTila = (model) => {
  return lensedModel(model, L.rewrite(m => {
    if (hasArvosana(m) && !suoritusValmis(m)) {
      return setTila(m, 'VALMIS')
    }
    return m
  }))
}

// model wrapper, joka poistaa arvioinnin, kun suorituksen tila muuttuu -> KESKEN
export const fixArvosana = (model) => {
  let arviointiLens = modelLens('arviointi')
  return lensedModel(model, L.rewrite(m => {
    var arviointiModel = L.get(arviointiLens, m)
    if (!suoritusValmis(m)) {
      return L.set(arviointiLens, createOptionalEmpty(arviointiModel), m)
    }
    return m
  }))
}