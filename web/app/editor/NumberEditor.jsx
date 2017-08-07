import React from 'react'
import {modelData, pushModelValue} from './EditorModel.js'
import {wrapOptional} from './OptionalEditor.jsx'
import {modelSetValue} from './EditorModel'

export class NumberEditor extends React.Component {
  render() {
    let {model} = this.props
    let wrappedModel = wrapOptional({model})
    let onChange = (event) => pushModelValue(wrappedModel, event.target.value ? { data: parseFloat(event.target.value) } : undefined)

    let data = modelData(wrappedModel)
    let value = data
      ? Math.round(data * 100) / 100
      : data

    let minValue = wrappedModel.minValue || (wrappedModel.minValueExclusive != undefined && wrappedModel.minValueExclusive + 1)
    let maxValue = wrappedModel.maxValue || (wrappedModel.maxValueExclusive != undefined && wrappedModel.maxValueExclusive - 1)

    return wrapWithUnitOfMeasure(wrappedModel.unitOfMeasure, wrappedModel.context.edit
      ? <input type="number" min={minValue} max={maxValue} defaultValue={modelData(wrappedModel)} onChange={ onChange } className="editor-input inline number"/>
      : <span className="inline number">{value}</span>)
  }
}

const wrapWithUnitOfMeasure = (unitOfMeasure, content) => unitOfMeasure ? <span>{content}<span className="unit-of-measure">{unitOfMeasure}</span></span> : content

NumberEditor.handlesOptional = () => true
NumberEditor.createEmpty = (m) => modelSetValue(m, undefined)
NumberEditor.validateModel = model => {
  let value = modelData(model)
  return (value !== undefined && isNaN(value)) ? [{key: 'invalid.number'}] : []
}
