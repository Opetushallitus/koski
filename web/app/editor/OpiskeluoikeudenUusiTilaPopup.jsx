import React from 'baret'
import Bacon from 'baconjs'
import {childContext, contextualizeModel, modelItems, modelLookup, accumulateModelState} from './EditorModel'
import {EnumEditor} from './EnumEditor.jsx'
import {DateEditor} from './DateEditor.jsx'
import ModalDialog from './ModalDialog.jsx'

export const OpiskeluoikeudenUusiTilaPopup = ({edellisenTilanAlkupäivä, suorituksiaKesken, tilaListModel, resultCallback}) => {
  let submitBus = Bacon.Bus()
  let initialModel = contextualizeModel(tilaListModel.arrayPrototype, childContext(tilaListModel.context, modelItems(tilaListModel).length))

  let { modelP, errorP } = accumulateModelState(initialModel)

  let alkuPäiväModel = modelP.map(m => modelLookup(m, 'alku'))
  let tilaModel = modelP.map(m => modelLookup(m, 'tila'))
  let validP = tilaModel.changes().map(true).toProperty(false).and(errorP.not())

  modelP.sampledBy(submitBus.filter(validP)).onValue(resultCallback)

  return (<ModalDialog className="lisaa-opiskeluoikeusjakso-modal" onDismiss={resultCallback} onSubmit={() => submitBus.push()}>
    <h2>Opiskeluoikeuden tilan lisäys</h2>
    <div className="property alku">
      <label>Päivämäärä:</label>
      <DateEditor baret-lift model={alkuPäiväModel} isAllowedDate={d => edellisenTilanAlkupäivä ? d >= edellisenTilanAlkupäivä : true }/>
    </div>
    <div className="property tila">
      <label>Tila:</label>
      <EnumEditor baret-lift asRadiogroup={true} model={tilaModel} disabledValue={suorituksiaKesken && 'valmistunut'} />
    </div>
    <button disabled={validP.not()} className="opiskeluoikeuden-tila button" onClick={() => submitBus.push()}>Lisää</button>
  </ModalDialog>)
}