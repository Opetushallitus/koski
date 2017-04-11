import {modelLookup, accumulateModelState, optionalPrototypeModel, modelSet, modelSetValue} from './EditorModel'
import React from 'baret'
import Bacon from 'baconjs'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import ModalDialog from './ModalDialog.jsx'
import {setTila} from './Suoritus'

export const MerkitseSuoritusValmiiksiPopup = ({ suoritus, resultCallback }) => {
  let submitBus = Bacon.Bus()
  let vahvistus = optionalPrototypeModel(modelLookup(suoritus, 'vahvistus'))
  suoritus = modelSet(suoritus, vahvistus, 'vahvistus')
  let toimipiste = modelLookup(suoritus, 'toimipiste')
  suoritus = modelSetValue(suoritus, toimipiste.value, 'vahvistus.myöntäjäOrganisaatio')
  suoritus = setTila(suoritus, 'VALMIS')
  let { modelP, errorP } = accumulateModelState(suoritus)
  let validP = errorP.not()
  modelP.sampledBy(submitBus).onValue(resultCallback)

  return (<ModalDialog className="merkitse-valmiiksi-modal" onDismiss={resultCallback} onSubmit={() => submitBus.push()}>
    <h2>Suoritus valmis</h2>
    <PropertiesEditor baret-lift model={modelP.map(s => modelLookup(s, 'vahvistus'))}  />
    <button disabled={validP.not()} onClick={() => submitBus.push()}>Merkitse valmiiksi</button>
  </ModalDialog>)
}
