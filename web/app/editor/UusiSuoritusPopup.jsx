import React from 'baret'
import Bacon from 'baconjs'
import {childContext, contextualizeModel, modelItems, modelLookup, accumulateModelState, modelSet} from './EditorModel'
import ModalDialog from './ModalDialog.jsx'
import {Editor} from './GenericEditor.jsx'

export default ({opiskeluoikeus, resultCallback}) => {
  let submitBus = Bacon.Bus()
  let suoritukset = modelLookup(opiskeluoikeus, 'suoritukset')
  var context = childContext(suoritukset.context, modelItems(suoritukset).length)
  let toimipiste = modelLookup(opiskeluoikeus, 'oppilaitos')

  let initialModel = contextualizeModel(suoritukset.arrayPrototype, context)

  if (initialModel.oneOfPrototypes) {
    let selectedProto = initialModel.oneOfPrototypes.find(p => p.key === 'perusopetuksenvuosiluokansuoritus')
    initialModel = contextualizeModel(selectedProto, context)
  }

  let withToimipiste = modelSet(initialModel, toimipiste, 'toimipiste')

  let { modelP, errorP } = accumulateModelState(withToimipiste)

  let validP = errorP.not()

  modelP.sampledBy(submitBus.filter(validP)).onValue(resultCallback)

  return (<ModalDialog className="lisaa-suoritus-modal" onDismiss={resultCallback} onSubmit={() => submitBus.push()}>
    <h2>Suorituksen lisäys</h2>
    <Editor baret-lift model={modelP} hideOptional={true}/>
    <button disabled={validP.not()} onClick={() => submitBus.push()}>Lisää</button>
  </ModalDialog>)
}