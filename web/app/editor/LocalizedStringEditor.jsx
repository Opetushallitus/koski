import React from 'react'
import R from 'ramda'
import {wrapOptional} from './OptionalEditor.jsx'
import {ObjectEditor} from './ObjectEditor.jsx'
import {StringEditor} from './StringEditor.jsx'
import {modelLookup} from './EditorModel.js'

export const LocalizedStringEditor = ({model}) => {
  if (!model.context.edit) {
    return <ObjectEditor model={model}/>
  }
  // TODO: handle multilingual strings
  let wrappedModel = wrapOptional({model})
  let stringModel = R.merge(modelLookup(wrappedModel, 'fi'), { optional: model.optional })

  return <StringEditor model={stringModel} />
}
LocalizedStringEditor.handlesOptional = true
LocalizedStringEditor.canShowInline = () => true
