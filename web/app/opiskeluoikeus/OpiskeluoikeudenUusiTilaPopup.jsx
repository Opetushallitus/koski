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
  const submitBus = Bacon.Bus()
  const initialModel = contextualizeSubModel(tilaListModel.arrayPrototype, tilaListModel, modelItems(tilaListModel).length)

  const { modelP, errorP } = accumulateModelStateAndValidity(initialModel)

  const isAllowedDate = d => edellisenTilanAlkupäivä ? d > edellisenTilanAlkupäivä : true

  const alkuPäiväModel = modelP.map(m => modelLookupRequired(m, 'alku'))
  const tilaModel = modelP.map(m => modelLookupRequired(m, 'tila'))
  const rahoitusModel = modelP.map(m => modelLookup(m, 'opintojenRahoitus'))

  const tilaSelectedP = tilaModel.changes().map(true).toProperty(false)
  const opintojenRahoitusValidP = Bacon.combineWith(validateOpintojenRahoitus, tilaModel, rahoitusModel)
  const validP = tilaSelectedP.and(opintojenRahoitusValidP).and(errorP.not())

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

const validateOpintojenRahoitus = (tilaModel, rahoitusModel) => {
  const tyyppi = tilaModel.parent.value.classes
  const tila = modelData(tilaModel, 'koodiarvo')
  const rahoitusValittu = modelData(rahoitusModel)

  if (tyyppi.includes('lukionopiskeluoikeusjakso') || tyyppi.includes('aikuistenperusopetuksenopiskeluoikeusjakso')) {
    return !(['lasna', 'valmistunut'].includes(tila)) || rahoitusValittu
  }
  if (tyyppi.includes('ammatillinenopiskeluoikeusjakso')) {
    return !(['lasna', 'valmistunut', 'loma'].includes(tila)) || rahoitusValittu
  }
  return true
}
