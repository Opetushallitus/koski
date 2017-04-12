import React from 'react'
import {modelData} from './EditorModel.js'
import {wrapOptional} from './OptionalEditor.jsx'
import {pushModelValue, modelValid} from './EditorModel'

export const StringEditor = ({model, placeholder}) => {
  let wrappedModel = wrapOptional({model})
  let onChange = (event) => pushModelValue(wrappedModel, { data: event.target.value })
  let data = modelData(model)
  let error = !modelValid(model)
  return model.context.edit
    ? <input className={error ? 'editor-input error' : 'editor-input valid'} type="text" defaultValue={data} placeholder={placeholder} onChange={ onChange }></input>
    : <span className="inline string">{!data ? '' : data.split('\n').map((line, k) => <span key={k}>{line}<br/></span>)}</span>
}

StringEditor.handlesOptional = true
StringEditor.canShowInline = () => true
StringEditor.validateModel = (model) => {
  return !model.optional && !modelData(model) ? ['empty string'] : []
}