import React from 'baret'
import Bacon from 'baconjs'
import {
  accumulateModelStateAndValidity,
  contextualizeSubModel, modelData,
  modelItems,
  modelLookup,
  modelLookupRequired,
  modelSetValue,
  pushModel
} from '../editor/EditorModel'
import {EnumEditor} from '../editor/EnumEditor'
import {Editor} from '../editor/Editor'
import ModalDialog from '../editor/ModalDialog'
import Text from '../i18n/Text'
import {ift} from '../util/util'
import {filterTilatByOpiskeluoikeudenJaSuorituksenTyyppi} from './opiskeluoikeus'
import {autoFillRahoitusmuoto, opiskeluoikeudenTilaVaatiiRahoitusmuodon, defaultRahoitusmuotoP} from './opintojenRahoitus'

export const OpiskeluoikeudenUusiTilaPopup = ({edellisenTilanAlkupäivä, disabloiValmistunut, tilaListModel, resultCallback}) => {
  const submitBus = Bacon.Bus()
  const initialModel = contextualizeSubModel(tilaListModel.arrayPrototype, tilaListModel, modelItems(tilaListModel).length)

  const { modelP, errorP } = accumulateModelStateAndValidity(initialModel)

  const isAllowedDate = d => edellisenTilanAlkupäivä ? d > edellisenTilanAlkupäivä : true

  const alkuPäiväModel = modelP.map(m => modelLookupRequired(m, 'alku'))
  const tilaModel = modelP.map(m => modelLookupRequired(m, 'tila'))
  const rahoitusModel = modelP.map(m => modelLookup(m, 'opintojenRahoitus'))

  const validP = errorP.not()

  const rahoitusmuotoChanges = Bacon.combineWith(tilaModel, rahoitusModel, defaultRahoitusmuotoP, (tilaM, rahoitusM, defaultRahoitus) => ({
    vaatiiRahoituksen: opiskeluoikeudenTilaVaatiiRahoitusmuodon(
      modelData(tilaM.context.opiskeluoikeus, 'tyyppi.koodiarvo'),
      modelData(tilaM, 'koodiarvo')
    ),
    rahoitusValittu: modelData(rahoitusM),
    setDefaultRahoitus: () => pushModel(modelSetValue(rahoitusM, defaultRahoitus)),
    setRahoitusNone: () => pushModel(modelSetValue(rahoitusM, null))
  }))

  rahoitusmuotoChanges.onValue(autoFillRahoitusmuoto)

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
      ift(rahoitusmuotoChanges.map(x => x.vaatiiRahoituksen), (<div className="property rahoitus" key="rahoitus">
        <label><Text name="Rahoitus"/>{':'}</label>
        <Editor baret-lift asRadiogroup={true} model={rahoitusModel}/>
      </div>))
    }
  </ModalDialog>)
}

const getKoodiarvo = t => t && t.data && t.data.koodiarvo
const fetchTilat = model => EnumEditor.fetchAlternatives(model).map(alts => {
  const opiskeluoikeudenTyyppi = modelData(model.context.opiskeluoikeus, 'tyyppi')
  const suorituksenTyyppi = modelLookup(model.context.opiskeluoikeus, 'suoritukset.0.tyyppi').value.data
  return filterTilatByOpiskeluoikeudenJaSuorituksenTyyppi(opiskeluoikeudenTyyppi, suorituksenTyyppi, getKoodiarvo)(alts)
})
