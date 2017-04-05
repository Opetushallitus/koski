import React from 'react'
import Bacon from 'baconjs'
import R from 'ramda'
import {wrapOptional, pushModelValue} from './OptionalEditor.jsx'
import {ObjectEditor} from './ObjectEditor.jsx'
import {StringEditor} from './StringEditor.jsx'
import {addContext, modelLookup} from './EditorModel.js'
import {modelEmpty} from './EditorModel'

export const LocalizedStringEditor = ({model}) => {
  let wrappedModel = wrapOptional({model, isEmpty: maybeEmptyModel => modelEmpty(maybeEmptyModel, 'fi')})

  let changeBus = Bacon.Bus()
  let subpath = 'fi'
  changeBus.onValue(([, stringModel]) => {
    pushModelValue(wrappedModel, stringModel.value, subpath)
  })
  if (!model.context.edit) {
    return <ObjectEditor model={model}/>
  }

  let stringModel = R.merge(addContext(modelLookup(wrappedModel, subpath), {changeBus}), { optional: model.optional })

  return <StringEditor model={stringModel} />
}
LocalizedStringEditor.handlesOptional = true
LocalizedStringEditor.canShowInline = () => true
