import {
  modelData,
  modelLookup,
  lensedModel,
  modelSetData
} from '../editor/EditorModel'
import * as L from 'partial.lenses'
import React from 'baret'
import { suoritusValmis } from './Suoritus'
import { Editor } from '../editor/Editor'
import Text from '../i18n/Text'
import { isEshS7 } from './SuoritustaulukkoCommon'

export const JääLuokalleTaiSiirretäänEditor = ({ model }) => {
  const jääLuokalleModel = modelLookup(model, 'jääLuokalle')
  if (!jääLuokalleModel) return null
  const jääLuokalle = modelData(jääLuokalleModel)
  const luokka = modelData(model, 'koulutusmoduuli.tunniste.koodiarvo')
  if (luokka && suoritusValmis(model)) {
    if (luokka === '9' || isEshS7(model)) {
      if (!model.context.edit && jääLuokalle) {
        return (
          <div className="jaa-tai-siirretaan">
            <Text name="Oppilas jää luokalle" />
          </div>
        )
      }
      return null
    } else if (model.context.edit) {
      const invertModelValue = (m) => modelSetData(m, !modelData(m))
      const invert = L.iso(invertModelValue, invertModelValue)
      return (
        <label className="jaa-tai-siirretaan">
          <Editor model={lensedModel(jääLuokalleModel, invert)} />{' '}
          <Text name="Siirretään seuraavalle luokalle" />
        </label>
      )
    } else {
      if (jääLuokalle === true) {
        return (
          <div className="jaa-tai-siirretaan">
            <Text name="Ei siirretä seuraavalle luokalle" />
          </div>
        )
      } else if (jääLuokalle === false) {
        return (
          <div className="jaa-tai-siirretaan">
            <Text name="Siirretään seuraavalle luokalle" />
          </div>
        )
      }
    }
  }
  return null
}
