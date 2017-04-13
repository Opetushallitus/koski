import {modelData, modelLookup, lensedModel, modelSetData} from './EditorModel'
import * as L from 'partial.lenses'
import React from 'baret'
import {suoritusValmis} from './Suoritus'
import {Editor} from './Editor.jsx'

export const JääLuokalleTaiSiirretäänEditor = ({model}) => {
  let jääLuokalleModel = modelLookup(model, 'jääLuokalle')
  if (!jääLuokalleModel) return null
  let jääLuokalle = modelData(jääLuokalleModel)
  let luokka = modelData(model, 'koulutusmoduuli.tunniste.koodiarvo')
  if (luokka && suoritusValmis(model)) {
    if (model.context.edit) {
      let invertModelValue = m => modelSetData(m, !modelData(m))
      let invert = L.iso(invertModelValue, invertModelValue)
      return <label className="jaa-tai-siirretaan"><Editor model={lensedModel(jääLuokalleModel, invert)} /> Siirretään seuraavalle luokalle</label>
    } else {
      if (jääLuokalle === true) {
        return <div className="jaa-tai-siirretaan">Ei siirretä seuraavalle luokalle</div>
      } else if (jääLuokalle === false && luokka !== '9') {
        return <div className="jaa-tai-siirretaan">Siirretään seuraavalle luokalle</div>
      }
    }
  }
  return null
}