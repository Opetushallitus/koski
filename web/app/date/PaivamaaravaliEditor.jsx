import React from 'react'
import { Editor } from '../editor/Editor'
import {
  modelData,
  modelEmpty,
  modelValid,
  wrapOptional
} from '../editor/EditorModel'

export const PäivämääräväliEditor = ({ model }) => {
  const wrappedModel = wrapOptional(model)
  const validRange = modelValid(wrappedModel, false)
  if (
    !model.context.edit &&
    modelEmpty(model, 'alku') &&
    modelEmpty(model, 'loppu')
  )
    return null

  return (
    <span className={validRange ? 'date-range' : 'date-range error'}>
      <span className="property alku">
        <Editor model={wrappedModel} path="alku" />
      </span>
      {' — '}
      <span className="property loppu">
        <Editor model={wrappedModel} path="loppu" />
      </span>
    </span>
  )
}

PäivämääräväliEditor.canShowInline = () => true
PäivämääräväliEditor.validateModel = (model) => {
  const alkuData = modelData(model, 'alku')
  const loppuData = modelData(model, 'loppu')
  if (!alkuData || !loppuData || new Date(alkuData) <= new Date(loppuData))
    return
  return [{ key: 'invalid.daterange' }]
}
