import React from 'react'
import {Editor} from './Editor.jsx'
import {modelData} from './EditorModel.js'
import {modelValid} from './EditorModel'
import {wrapOptional} from './OptionalEditor.jsx'

export const PäivämääräväliEditor = ({model}) => {
  let wrappedModel = wrapOptional({model})
  let validRange = modelValid(wrappedModel)

  return (<span className={validRange ? 'date-range' : 'date-range error'}>
    <Editor model={wrappedModel} path="alku"/> — <Editor model={wrappedModel} path="loppu"/>
  </span>)
}

PäivämääräväliEditor.canShowInline = () => true
PäivämääräväliEditor.handlesOptional = true
PäivämääräväliEditor.validateModel = (model) => {
  let alkuData = modelData(model, 'alku')
  let loppuData = modelData(model, 'loppu')
  if (!alkuData || !loppuData || new Date(alkuData) <= new Date(loppuData)) return
  return [{key: 'invalid.daterange'}]
}