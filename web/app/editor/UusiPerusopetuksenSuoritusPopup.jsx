import React from 'baret'
import Bacon from 'baconjs'
import R from 'ramda'
import * as L from 'partial.lenses'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {
  childContext,
  contextualizeModel,
  modelItems,
  modelLookup,
  accumulateModelState,
  modelSet,
  addContext,
  modelData,
  modelLens,
  modelProperties
} from './EditorModel'
import ModalDialog from './ModalDialog.jsx'

const UusiPerusopetuksenSuoritusPopup = ({opiskeluoikeus, resultCallback}) => {
  let submitBus = Bacon.Bus()
  let suoritukset = modelLookup(opiskeluoikeus, 'suoritukset')
  var context = childContext(suoritukset.context, modelItems(suoritukset).length)
  let selectedProto = contextualizeModel(suoritukset.arrayPrototype, context).oneOfPrototypes.find(p => p.key === 'perusopetuksenvuosiluokansuoritus')
  let initialModel = contextualizeModel(selectedProto, context)
  initialModel = L.modify(L.compose(modelLens('koulutusmoduuli.tunniste'), 'alternativesPath'), (url => url + '/' + puuttuvatLuokkaAsteet(opiskeluoikeus).join(',')) , initialModel)

  let viimeisin = viimeisinLuokkaAste(opiskeluoikeus)
  if (viimeisin) {
    initialModel = modelSet(initialModel, modelLookup(viimeisin, 'toimipiste'), 'toimipiste')
  }

  initialModel = addContext(initialModel, { editAll: true })

  let { modelP, errorP } = accumulateModelState(initialModel)

  let hasToimipisteP = modelP.map(m => !!modelData(m, 'toimipiste.oid'))
  let validP = errorP.not().and(hasToimipisteP)

  modelP.sampledBy(submitBus.filter(validP)).onValue(resultCallback)

  return (<ModalDialog className="lisaa-suoritus-modal" onDismiss={resultCallback} onSubmit={() => submitBus.push()}>
    <h2>Suorituksen lisäys</h2>
    <PropertiesEditor baret-lift context={initialModel.context} properties={modelP.map(model => modelProperties(model, ['koulutusmoduuli.tunniste', 'luokka', 'toimipiste']))} />
    <button disabled={validP.not()} onClick={() => submitBus.push()}>Lisää</button>
  </ModalDialog>)
}
UusiPerusopetuksenSuoritusPopup.canAddSuoritus = (opiskeluoikeus) => {
  let tyyppi = modelData(opiskeluoikeus, 'tyyppi.koodiarvo')
  return tyyppi == 'perusopetus' && puuttuvatLuokkaAsteet(opiskeluoikeus).length > 0
}
export default UusiPerusopetuksenSuoritusPopup

let puuttuvatLuokkaAsteet = (opiskeluoikeus) => {
  var olemassaOlevatLuokkaAsteet = olemassaolevatLuokkaAsteenSuoritukset(opiskeluoikeus).map(suorituksenLuokkaAste)
  return [1, 2, 3, 4, 5, 6, 7, 8, 9].filter(x => !olemassaOlevatLuokkaAsteet.includes(x))
}

let olemassaolevatLuokkaAsteenSuoritukset = (opiskeluoikeus) => modelItems(opiskeluoikeus, 'suoritukset')
  .filter(suoritus => modelData(suoritus, 'tyyppi.koodiarvo') == 'perusopetuksenvuosiluokka')

let suorituksenLuokkaAste = (suoritus) => parseInt(modelData(suoritus, 'koulutusmoduuli.tunniste.koodiarvo'))

let viimeisinLuokkaAste = (opiskeluoikeus) => {
  let suoritukset = olemassaolevatLuokkaAsteenSuoritukset(opiskeluoikeus)
  if (suoritukset.length) {
    return suoritukset.reduce(R.maxBy(suorituksenLuokkaAste))
  }
}