import React from 'react'
import {modelData} from './EditorModel.ts'
import {wrapOptional} from './EditorModel'
import {pushModelValue, modelValid} from './EditorModel'
import {t} from '../i18n/i18n'

export const StringEditor = ({model, placeholder, autoFocus}) => {
  placeholder = !placeholder && model.example ? `${t('Esimerkki')}: ${model.example}` : placeholder
  let wrappedModel = wrapOptional(model)
  let onChange = (event) => pushModelValue(wrappedModel, { data: event.target.value })
  let data = modelData(model)
  let error = !modelValid(model)
  let className = 'editor-input ' + (error ? 'error' : 'valid')
  return model.context.edit
    ? (model.maxLines
      ? <textarea className={className} defaultValue={data} placeholder={placeholder} onChange={ onChange } rows={ model.maxLines } autoFocus={autoFocus}></textarea>
      : <input className={className} type="text" defaultValue={data} placeholder={placeholder} onChange={ onChange } autoFocus={autoFocus}></input>)
    : <span className="inline string">{!data ? '' : splitToRows(data)}</span>
}

let splitToRows = (data) => data.replace(/\r\n*/g, '\n').split('\n').map((line, k) => <span key={k}>{k > 0 ? <br/> : null}{line}</span>)

const buildRegex = model => new RegExp(model.regularExpression)

StringEditor.handlesOptional = () => true
StringEditor.isEmpty = m => !modelData(m)
StringEditor.canShowInline = () => true
StringEditor.validateModel = (model) => {
  let data = modelData(model)
  if (!model.optional && !data) {
    return [{key: 'missing'}]
  }

  if (data && model.regularExpression && !buildRegex(model).test(data)) {
    return [{key: 'invalid.format'}]
  }
}
