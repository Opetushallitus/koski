import React from 'baret'
import Bacon from 'baconjs'
import {
  accumulateModelStateAndValidity,
  contextualizeSubModel, modelData,
  modelItems,
  modelLookup,
  modelLookupRequired
} from '../editor/EditorModel'
import {EnumEditor} from '../editor/EnumEditor'
import {Editor} from '../editor/Editor'
import ModalDialog from '../editor/ModalDialog'
import Text from '../i18n/Text'
import {ift} from '../util/util'
import {filterTilatByOpiskeluoikeudenTyyppi} from './opiskeluoikeus'

export const OpiskeluoikeudenUusiTilaPopup = ({edellisenTilanAlkupäivä, disabloiValmistunut, tilaListModel, resultCallback}) => {
  let submitBus = Bacon.Bus()
  let initialModel = contextualizeSubModel(tilaListModel.arrayPrototype, tilaListModel, modelItems(tilaListModel).length)

  let { modelP, errorP } = accumulateModelStateAndValidity(initialModel)

  let isAllowedDate = d => edellisenTilanAlkupäivä ? d > edellisenTilanAlkupäivä : true

  let alkuPäiväModel = modelP.map(m => modelLookupRequired(m, 'alku'))
  let tilaModel = modelP.map(m => modelLookupRequired(m, 'tila'))
  let rahoitusModel = modelP.map(m => modelLookup(m, 'opintojenRahoitus'))
  let tilaSelectedP = tilaModel.changes().map(true).toProperty(false)
  let validP = tilaSelectedP.and(errorP.not())

  modelP.sampledBy(submitBus.filter(validP)).onValue(resultCallback)


  return (<ModalDialog className="lisaa-opiskeluoikeusjakso-modal" onDismiss={resultCallback} onSubmit={() => submitBus.push()} okTextKey="Lisää" validP={validP}>
    <h2><Text name="Opiskeluoikeuden tilan lisäys"/></h2>
    <div className="property alku">
      <label><Text name="Päivämäärä"/>{':'}</label>
      <Editor baret-lift model={alkuPäiväModel} isAllowedDate={isAllowedDate}/>
    </div>
    <div className="property tila">
      <label><Text name="Tila"/>{':'}</label>
      <Editor baret-lift asRadiogroup={true} model={tilaModel} disabledValue={disabloiValmistunut && 'koskiopiskeluoikeudentila_valmistunut'} fetchAlternatives={fetchTilat} />
    </div>
    {
      ift(rahoitusModel, (<div className="property rahoitus" key="rahoitus">
        <label><Text name="Rahoitus"/>{':'}</label>
        <Editor baret-lift asRadiogroup={true} model={rahoitusModel}/>
      </div>))
    }
  </ModalDialog>)
}

const getKoodiarvo = t => t && t.data && t.data.koodiarvo
const fetchTilat = model => EnumEditor.fetchAlternatives(model).map(alts => {
  const tyyppi = modelData(model.context.opiskeluoikeus, 'tyyppi')
  return filterTilatByOpiskeluoikeudenTyyppi(tyyppi, getKoodiarvo)(alts)
})

