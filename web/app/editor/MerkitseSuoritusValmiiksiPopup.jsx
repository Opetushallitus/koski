import {
  modelLookup,
  accumulateModelState,
  optionalPrototypeModel,
  modelSet,
  modelSetValue,
  modelItems,
  modelData,
  addContext
} from './EditorModel'
import R from 'ramda'
import React from 'baret'
import Bacon from 'baconjs'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import ModalDialog from './ModalDialog.jsx'
import {setTila} from './Suoritus'
import Http from '../http'
import {JääLuokalleTaiSiirretäänEditor} from './JääLuokalleTaiSiirretäänEditor.jsx'

export const MerkitseSuoritusValmiiksiPopup = ({ suoritus, resultCallback }) => {
  let submitBus = Bacon.Bus()
  let vahvistus = optionalPrototypeModel(modelLookup(suoritus, 'vahvistus'))
  suoritus = modelSet(suoritus, vahvistus, 'vahvistus')
  let toimipiste = modelLookup(suoritus, 'toimipiste')
  suoritus = modelSetValue(suoritus, toimipiste.value, 'vahvistus.myöntäjäOrganisaatio')
  suoritus = setTila(suoritus, 'VALMIS')
  let { modelP, errorP } = accumulateModelState(suoritus)
  let validP = errorP.not()

  modelP.sampledBy(submitBus).onValue(updatedSuoritus => {
    let saveResults = modelItems(updatedSuoritus, 'vahvistus.myöntäjäHenkilöt').filter(h => h.value.newItem).map(h => {
      let data = modelData(h)
      let key = data.nimi
      let organisaatioOid = modelData(updatedSuoritus, 'toimipiste').oid
      var path = `/koski/api/preferences/${organisaatioOid}/myöntäjät`
      return Http.put(path, { key, value: data}, { invalidateCache: [path] })
    })
    Bacon.combineAsArray(saveResults).onValue(() => resultCallback(updatedSuoritus))
  })

  return (<ModalDialog className="merkitse-valmiiksi-modal" onDismiss={resultCallback} onSubmit={() => submitBus.push()}>
    <h2>Suoritus valmis</h2>
    <PropertiesEditor baret-lift model={modelP.map(s => setOrgToContext(modelLookup(s, 'vahvistus')))}  />
    <JääLuokalleTaiSiirretäänEditor baret-lift model={modelP}/>
    <button disabled={validP.not()} onClick={() => submitBus.push()}>Merkitse valmiiksi</button>
  </ModalDialog>)
}

let setOrgToContext = (vahvistus) => {
  let myöntäjäOrganisaatio = modelLookup(vahvistus, 'myöntäjäOrganisaatio')
  return addContext(vahvistus, { myöntäjäOrganisaatio })
}