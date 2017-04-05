import React from 'react'
import {Editor} from './Editor.jsx'
import {contextualizeModel, addContext, modelData, modelLookup, childContext, modelSet} from './EditorModel.js'
import Bacon from 'baconjs'
import {modelValid, modelEmpty} from './EditorModel'
import {resetOptionalModel} from './OptionalEditor.jsx'

export const PäivämääräväliEditor = ({model}) => {
  let alkuPäiväBus = Bacon.Bus(),
      loppuPäiväBus = Bacon.Bus()

  let usedModel = model.value ? model : model.optional ? contextualizeModel(model.optionalPrototype, model.context) : model

  let initialChangeEventFromModel = (path) => {
    return [childContext(usedModel.context, path), modelLookup(usedModel, path)]
  }

  let rangeP = Bacon.combineTemplate({
    alku: alkuPäiväBus.toProperty(initialChangeEventFromModel('alku')),
    loppu: loppuPäiväBus.toProperty(initialChangeEventFromModel('loppu'))
  })

  rangeP.changes().onValue(({alku, loppu}) => {
    if (modelEmpty(alku[1]) && modelEmpty(loppu[1]) && model.optional) {
      resetOptionalModel(model)
    } else {
      let withAlku = modelSet(usedModel, alku[1], 'alku')
      let withLoppu = modelSet(withAlku, loppu[1], 'loppu')
      let context = model.context
      var values = [context, withLoppu]
      model.context.changeBus.push(values)
    }
  })

  let validRange = modelValid(usedModel)

  return (<span className={validRange ? 'date-range' : 'date-range error'}>
    <Editor model={addContext(usedModel, {changeBus: alkuPäiväBus})} path="alku"/> — <Editor model={addContext(usedModel, {changeBus: loppuPäiväBus})} path="loppu"/>
  </span>)
}

PäivämääräväliEditor.canShowInline = () => true
PäivämääräväliEditor.handlesOptional = true
PäivämääräväliEditor.validateModel = (model) => {
  let alkuData = modelData(model, 'alku')
  let loppuData = modelData(model, 'loppu')
  if (!alkuData || !loppuData || new Date(alkuData) <= new Date(loppuData)) return
  return ['invalid range']
}