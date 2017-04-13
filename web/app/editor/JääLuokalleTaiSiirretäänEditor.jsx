import {modelData} from './EditorModel'
import React from 'baret'
import {suoritusValmis} from './Suoritus'

export const JääLuokalleTaiSiirretäänEditor = ({model}) => {
  let jääLuokalle = modelData(model, 'jääLuokalle')
  let luokka = modelData(model, 'koulutusmoduuli.tunniste.koodiarvo')
  if (luokka && suoritusValmis(model)) {
    if (jääLuokalle === true) {
      return <div>Ei siirretä seuraavalle luokalle</div>
    } else if (jääLuokalle === false && luokka !== '9') {
      return <div>Siirretään seuraavalle luokalle</div>
    }
  }
  return null
}