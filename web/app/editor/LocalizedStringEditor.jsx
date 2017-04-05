import R from 'ramda'
import React from 'react'
import Bacon from 'baconjs'
import {optionalModel, pushOptionalModelValue} from './OptionalEditor.jsx'
import {ObjectEditor} from './ObjectEditor.jsx'
import {StringEditor} from './StringEditor.jsx'
import {addContext, modelLookup, modelTitle} from './EditorModel.js'

export const LocalizedStringEditor = ({model}) => {
  let changeBus = Bacon.Bus()
  let subpath = 'fi'
  changeBus.onValue(([c, stringModel]) => {
    pushOptionalModelValue(model, stringModel.value, subpath)
  })
  if (!model.context.edit) {
    return <ObjectEditor model={model}/>
  }

  let stringModel = addContext(modelLookup(model.optional ? optionalModel(model) : model, subpath), {changeBus})
  return <StringEditor model={model.optional ? R.merge(stringModel, {value: {data: modelTitle(model)}, optional: true}) : stringModel} />
}
LocalizedStringEditor.handlesOptional = true
LocalizedStringEditor.canShowInline = () => true
