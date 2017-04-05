import R from 'ramda'
import React from 'react'
import Bacon from 'baconjs'
import {optionalModel, resetOptionalModel} from './OptionalEditor.jsx'
import {ObjectEditor} from './ObjectEditor.jsx'
import {StringEditor} from './StringEditor.jsx'
import {addContext, modelLookup, modelData, modelSet, modelTitle} from './EditorModel.js'
import { delays } from '../delays'

export const LocalizedStringEditor = ({model}) => {
  let valueBus = Bacon.Bus()
  valueBus.debounce(delays().stringInput).onValue(([context,stringModel]) => {
    if (!modelData(stringModel)) {
      resetOptionalModel(model)
    } else {
      let myModel = model.optional ? optionalModel(model) : model
      let subpath = context.path.substring(model.context.path.length + 1)
      let updatedModel = modelSet(myModel, stringModel, subpath)
      model.context.changeBus.push([model.context, updatedModel])
    }
  })
  if (!model.context.edit) {
    return <ObjectEditor model={model}/>
  }

  let stringModel = addContext(modelLookup(model.optional ? optionalModel(model) : model, 'fi'), {changeBus: valueBus})
  return <StringEditor model={model.optional ? R.merge(stringModel, {value: {data: modelTitle(model)}, optional: true}) : stringModel} />
}
LocalizedStringEditor.handlesOptional = true
LocalizedStringEditor.canShowInline = () => true
