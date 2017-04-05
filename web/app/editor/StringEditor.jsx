import React from 'react'
import {modelData} from './EditorModel.js'
import {pushOptionalModelValue} from './OptionalEditor.jsx'
import {modelValid} from './EditorModel'

export const StringEditor = ({model}) => {
  let onChange = (event) => {
    let value = event.target.value
    value = value ? {data: value} : null
    pushOptionalModelValue(model, value)
  }

  let data = modelData(model)
  let error = !modelValid(model)
  return model.context.edit
    ? <input className={error ? 'editor-input error' : 'editor-input valid'} type="text" defaultValue={data} onChange={ onChange }></input>
    : <span className="inline string">{!data ? '' : data.split('\n').map((line, k) => <span key={k}>{line}<br/></span>)}</span>
}
StringEditor.handlesOptional = true
StringEditor.canShowInline = () => true
StringEditor.validateModel = (model) => {
  return !model.optional && !modelData(model) ? ['empty string'] : []
}