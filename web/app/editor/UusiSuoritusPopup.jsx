import React from 'baret'
import Bacon from 'baconjs'
import R from 'ramda'
import * as L from 'partial.lenses'
import { PropertiesEditor } from './PropertiesEditor.jsx'
import {
  childContext,
  contextualizeModel,
  modelItems,
  modelLookup,
  accumulateModelState,
  modelSet,
  addContext,
  modelData
} from './EditorModel'
import ModalDialog from './ModalDialog.jsx'
import {Editor} from './GenericEditor.jsx'
import {modelLens} from './EditorModel';

export default ({opiskeluoikeus, resultCallback}) => {
  let submitBus = Bacon.Bus()
  let suoritukset = modelLookup(opiskeluoikeus, 'suoritukset')
  var context = childContext(suoritukset.context, modelItems(suoritukset).length)
  let toimipiste = R.merge(modelLookup(opiskeluoikeus, 'oppilaitos'), { optional: false, optionalPrototypes: undefined })


  let initialModel = contextualizeModel(suoritukset.arrayPrototype, context)

  if (initialModel.oneOfPrototypes) {
    let selectedProto = initialModel.oneOfPrototypes.find(p => p.key === 'perusopetuksenvuosiluokansuoritus')

    let olemassaolevatLuokkaAsteet = modelItems(opiskeluoikeus, 'suoritukset')
      .filter(suoritus => modelData(suoritus, 'tyyppi.koodiarvo') == 'perusopetuksenvuosiluokka')
      .map(suoritus => parseInt(modelData(suoritus, 'koulutusmoduuli.tunniste.koodiarvo')))

    let puuttuvatLuokkaAsteet = [1,2,3,4,5,6,7,8,9].filter(x => !olemassaolevatLuokkaAsteet.includes(x))

    initialModel = contextualizeModel(selectedProto, context)
    initialModel = L.modify(L.compose(modelLens('koulutusmoduuli.tunniste'), 'alternativesPath'), (url => url + '/' + puuttuvatLuokkaAsteet.join(',')) , initialModel)
  }

  let withToimipiste = modelSet(initialModel, toimipiste, 'toimipiste')
  let withEditAll = addContext(withToimipiste, { editAll: true })

  let { modelP, errorP } = accumulateModelState(withEditAll)

  let validP = errorP.not()

  modelP.sampledBy(submitBus.filter(validP)).onValue(resultCallback)

  return (<ModalDialog className="lisaa-suoritus-modal" onDismiss={resultCallback} onSubmit={() => submitBus.push()}>
    <h2>Suorituksen lisäys</h2>
    <PropertiesEditor baret-lift
                      model={modelP}
                      propertyFilter={property => !property.model.optional && !['tila', 'jääLuokalle'].includes(property.key)}
                      />
    <button disabled={validP.not()} onClick={() => submitBus.push()}>Lisää</button>
  </ModalDialog>)
}