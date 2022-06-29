import React from 'react'
import {Editor} from '../editor/Editor'
import {modelData} from '../editor/EditorModel'
import {modelEmpty, modelValid, wrapOptional} from '../editor/EditorModel'

export const PäivämääräväliEditor = ({model}) => {
  let wrappedModel = wrapOptional(model)
  let validRange = modelValid(wrappedModel, false)
  if (!model.context.edit && modelEmpty(model, 'alku') && modelEmpty(model, 'loppu')) return null

  return (<span className={validRange ? 'date-range' : 'date-range error'}>
    <span className="property alku"><Editor model={wrappedModel} path="alku"/></span>{' — '}<span className="property loppu"><Editor model={wrappedModel} path="loppu"/></span>
  </span>)
}

PäivämääräväliEditor.canShowInline = () => true
PäivämääräväliEditor.validateModel = (model) => {
  let alkuData = modelData(model, 'alku')
  let loppuData = modelData(model, 'loppu')
  if (!alkuData || !loppuData || new Date(alkuData) <= new Date(loppuData)) return
  return [{key: 'invalid.daterange'}]
}